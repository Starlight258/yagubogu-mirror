# GameCompletedEvent Transactional Outbox 설계

## 1. 목적

현재 경기 종료 감지 이벤트는 `crawling` 애플리케이션에서 Spring 로컬 이벤트로만 발행되고 있습니다.

```text
crawling
-> bronze_games_raw 저장
-> ApplicationEventPublisher.publishEvent(GameFinalizedEvent)
-> 같은 JVM 안의 listener만 수신
```

이 구조에서는 `app`과 `crawling`을 각각 다른 JVM이나 별도 서버에 배포하게 되면, `app`에서 필요한 후속 작업이 해당 이벤트를 받을 수 없습니다.

그래서 해결하고자 하는 목표는 "**crawling이 감지한 경기 완료 사실을 DB에 안전하게 기록하고, app의 승부 예측 정산과 랭킹 갱신이 장애나 재시작이 있더라도 누락 없이 진행되도록 한다.**" 입니다.

이를 위해 `bronze_games_raw` 저장과 `outbox_events` 저장을 같은 DB 트랜잭션으로 묶는 Transactional Outbox 패턴을 적용합니다.

## 2. 현재 상태

지금은 경기 종료를 감지하는 흐름이 아래와 같이 동작합니다.

```java
KboScoreboardService.updateFromScoreboard()
-> bronzeGameService.upsertByNaturalKey(...)
-> state가 COMPLETED 또는 CANCELED이면 GameFinalizedEvent 발행
```

여기서 중요한 점은, `GameFinalizedEvent`가 데이터베이스에는 저장되지 않는다는 점입니다.

현재 이와 관련된 테이블은 다음과 같습니다.

| 테이블                | 역할                              |
|-----------------------|-----------------------------------|
| `bronze_games_raw`    | KBO 스코어보드 원본 JSON 데이터 저장 |
| `review_crawl_retries`| 리뷰 크롤링 재시도 작업 큐          |

`review_crawl_retries`는 경기 완료 이벤트 자체를 저장하는 outbox 역할을 하지 않습니다. 이 테이블은 리뷰 크롤링 작업을 예약하고 재시도하는 영속 작업 큐입니다.

즉, 지금 방식은 Transactional Outbox 패턴을 적용한 구조가 아닙니다.

```text
현재 구조
- bronze_games_raw 저장: DB 트랜잭션
- GameFinalizedEvent 발행: 스프링 로컬 이벤트로 처리
- 이벤트 자체 저장: 없음
- 리뷰 큐 저장: AFTER_COMMIT 리스너에서 별도의 트랜잭션으로 처리
```

## 3. 적용 범위

이 문서에서는 `GameCompletedEvent`에만 Transactional Outbox를 적용합니다.

> **이름 기준:** 기존 코드의 `GameFinalizedEvent`는 `COMPLETED`와 `CANCELED`를 모두 포함하는 최종 상태처럼 읽힙니다. 하지만 이 outbox는 승부 예측과 랭킹 갱신에 필요한 `COMPLETED` 경기만 저장합니다. 따라서 DB 메시지 계약의 이벤트 이름은 `GameCompletedEvent`, `event_type` 값은 `GAME_COMPLETED`로 분리하는 편이 정확합니다.

`REVIEW_CRAWL_QUEUE`는 outbox 적용 대상에서 제외합니다. `review_crawl_retries` 자체가 polling, 상태 관리, 재시도를 담당하는 영속 작업 큐이고, 경기 데이터 저장과 같은 트랜잭션에서 직접 insert할 수 있기 때문입니다.

## 4. 목표 구조

`bronze_games_raw`와 `outbox_events` 테이블에 대한 저장 작업을 동일한 DB 트랜잭션 안에서 처리하도록 구성한다.

```text
transaction begin
  1. bronze_games_raw upsert
  2. 경기 상태가 COMPLETED일 때만 outbox_events insert
transaction commit
```

`GAME_ETL` 작업은 이 outbox를 따로 통하지 않는다. `GameEtlScheduler`가 1분마다 전체 경기를 스캔하면서 이미 처리하기 때문에(§8.1 참고), 별도의 전달 경로가 필요하지 않다. 그리고 `CANCELED` 상태의 경기는 승부 예측 정산과 랭킹 갱신 대상이 아니어서 outbox row 자체를 만들지 않는다.

트랜잭션 커밋 이후에는 `app` worker가 `outbox_events`를 주기적으로 polling하여 랭킹 갱신을 시도한다. 경기 완료 이벤트가 하루에 몇 건 수준인 현재 요구사항에서는 별도의 wake-up signal 없이 5~10초 단위 polling만으로 충분하다.

```text
outbox_events (COMPLETED만)
-> app worker polling
   -> 승부 예측 점수 정산
   -> 주간 승부 예측 랭킹 갱신
   -> 필요하면 승리요정 랭킹 갱신
```

`REVIEW_CRAWL_QUEUE` 역시 이 outbox를 거치지 않는다(§8.3 참조).

이 구조에서 outbox는 '이벤트 저장소'의 역할이고, worker는 '전달 및 처리자'로 동작한다.

### 4.1 이벤트 전달 방식 선택

Transactional Outbox는 "원본 데이터 변경과 발행할 이벤트 저장을 같은 DB 트랜잭션으로 묶는 것"까지가 핵심이다. 저장된 이벤트를 어떻게 소비할지는 별도 설계 결정이다.

| 구현 방식 | 판단 |
| --- | --- |
| 커밋 후 동기/비동기 호출 | 선택하지 않는다. `app`과 `crawling`이 분리된 프로세스라 전달 수단이 되지 않고, commit 직후 프로세스가 죽으면 메모리 이벤트가 유실된다. |
| DB polling worker | 이번 설계의 선택이다. `app` worker가 `outbox_events`를 직접 조회하므로 별도 인프라 없이 누락 복구와 실패 상태 관리가 가능하다. |
| after-commit signal + DB polling | 현재는 선택하지 않는다. 빠른 반영에는 유리하지만 HTTP, Redis Pub/Sub, MQ 같은 외부 signal 전달 수단이 추가된다. |
| CDC 또는 메시지 브로커 연동 | 현재는 선택하지 않는다. 이벤트 종류와 소비자가 적어 운영 복잡도 대비 이득이 작다. 이벤트 수나 consumer가 늘면 다시 검토한다. |

현재 요구사항에서는 경기 완료 이벤트가 하루 몇 건 수준이므로 5~10초 단위 DB polling만으로 충분하다. wake-up signal은 실제 polling 지연이 문제가 될 때 별도 설계로 추가한다.

### 4.2 Worker 위치 선택

Outbox worker는 `app`에 둘 수도 있고 `crawling`에 둘 수도 있다. 둘 다 같은 공유 DB의 `outbox_events`를 polling할 수 있으므로, 단순히 "어느 프로세스에서 outbox row를 읽을 것인가"만 보면 둘 다 가능하다. 차이는 후속 작업을 어떤 경계로 실행하느냐에 있다.

| Worker 위치 | 후속 작업 실행 방식 | 장점 | 단점 |
| --- | --- | --- | --- |
| `app` worker | app 내부에서 승부 예측 정산 서비스와 랭킹 갱신 서비스를 직접 호출 | 경기 완료 후속 처리 도메인을 소유한 프로세스에서 처리하므로 책임 경계가 명확하다. API 호출 없이 내부 서비스 호출로 처리할 수 있다. | 최대 polling 주기만큼 지연될 수 있다. 현재는 5~10초 수준으로 제한한다. |
| `crawling` worker + app API 호출 | crawling이 outbox를 읽고 app의 내부/admin API를 호출 | crawling이 경기 종료 감지 직후 처리 트리거까지 담당할 수 있어 흐름이 직관적이다. app과 완전히 분리된 배포에서도 HTTP 경계로 실행할 수 있다. | API 호출 실패, 인증, timeout, idempotency, API 계약 관리가 추가된다. 결국 outbox 재시도와 HTTP 재시도를 함께 설계해야 한다. |
| `crawling` worker + app 서비스 직접 import | crawling 프로세스가 app의 service/repository bean을 import해 직접 실행 | API hop 없이 같은 DB 트랜잭션 또는 내부 메서드 호출처럼 구성할 수 있다. 현재 crawling도 일부 app bean과 repository를 import하고 있어 기술적으로는 가능하다. | crawling이 stat/checkin/ranking 도메인까지 알게 되어 모듈 경계가 흐려진다. app 쪽 서비스 의존성이 늘수록 crawling 배포 단위가 무거워지고, 장애 범위도 커진다. |

이번 설계에서는 **`app` worker**를 선택한다. 승부 예측 정산과 랭킹 갱신은 app 도메인이므로, crawling은 `bronze_games_raw`, `outbox_events`, `review_crawl_retries`를 같은 트랜잭션으로 저장하는 발행자 역할까지만 맡고 app이 후속 처리를 실행한다.

## 5. 테이블 설계

outbox의 논리적 consumer는 app의 `GAME_COMPLETED 후속 처리 worker` 하나다. 이 worker는 우선 승부 예측 정산과 주간 승부 예측 랭킹 갱신을 처리하고, 필요하면 승리요정 랭킹 갱신도 함께 실행한다. `GAME_ETL`의 경우는 `GameEtlScheduler`가 1분 단위로 맡아서 처리하므로, outbox를 거치지 않는다(§8.1 참고). consumer 종류가 하나이기 때문에, 이벤트 원본과 소비 상태를 분리하지 않고 `outbox_events` 테이블 하나에 상태 정보를 그대로 둔다.

### 5.1 사용하는 테이블

```sql
CREATE TABLE outbox_events
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Outbox 이벤트 식별자',
    event_type    VARCHAR(100) NOT NULL COMMENT '이벤트 종류. 현재는 GAME_COMPLETED',
    aggregate_id  VARCHAR(100) NOT NULL COMMENT '이벤트 대상 식별자. 현재는 gameCode',
    payload       JSON         NOT NULL COMMENT '이벤트 처리에 필요한 데이터 JSON',
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태',
    retry_count   INT          NOT NULL DEFAULT 0 COMMENT '처리 실패 후 재시도 횟수',
    next_retry_at DATETIME(6)  NOT NULL COMMENT '다음 처리 시도 가능 시각',
    processed_at  DATETIME(6)  NULL COMMENT '처리 성공 시각',
    last_error    TEXT         NULL COMMENT '마지막 처리 실패 원인',
    created_at    DATETIME(6)  NOT NULL COMMENT '이벤트 생성 시각',
    updated_at    DATETIME(6)  NOT NULL COMMENT '마지막 상태 변경 시각',

    UNIQUE KEY uk_outbox_event (event_type, aggregate_id),
    INDEX idx_outbox_polling (status, next_retry_at, id),
    INDEX idx_outbox_recovery (status, updated_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

`aggregate_id`는 `gameCode`를 사용한다.

```text  
event_type = GAME_COMPLETED
aggregate_id = {gameCode}  
```  

각 컬럼의 역할은 다음과 같다.

| 컬럼 | 설명 |
| --- | --- |
| `id` | outbox row의 내부 PK |
| `event_type` | 메시지 계약 값. 현재는 `GAME_COMPLETED` |
| `aggregate_id` | 이벤트 대상 식별자. 현재는 `gameCode` |
| `payload` | worker가 처리에 사용할 JSON 데이터. 현재는 `gameCode`, `date` |
| `status` | `PENDING`, `PROCESSING`, `PROCESSED`, `FAILED` |
| `retry_count` | 처리 실패 후 재시도 횟수 |
| `next_retry_at` | 다음 처리 시도 가능 시각 |
| `processed_at` | 정상 처리 완료 시각 |
| `last_error` | 마지막 실패 원인 |
| `created_at` | 이벤트 생성 시각 |
| `updated_at` | 마지막 상태 변경 시각. stuck `PROCESSING` 복구 기준 |

`next_retry_at`과 `updated_at`은 역할이 다르다.

```text
next_retry_at = 언제부터 다시 처리해도 되는가
updated_at    = 이 row의 상태가 마지막으로 언제 바뀌었는가
```

예를 들어 처리 실패 후 5분 뒤 재시도한다면 `updated_at`은 실패가 기록된 시각이고, `next_retry_at`은 5분 뒤가 된다. 반대로 worker가 `PROCESSING`으로 바꾼 뒤 죽은 경우에는 `updated_at`을 기준으로 stuck row를 찾는다.

### 5.2 제외하는 항목

현재는 `GAME_COMPLETED` 이벤트와 app 후속 처리 worker만 다루므로 다음 항목은 만들지 않는다.

| 제외 항목 | 제외 이유 |
| --- | --- |
| `outbox_event_consumptions` 테이블 | consumer가 하나라 소비 상태를 분리하지 않는다. |
| `consumer_name`, `outbox_event_id` | consumption 테이블을 두지 않으므로 필요 없다. |
| `aggregate_type` | 현재 이벤트 대상은 항상 `GAME`이다. |
| `state` | `COMPLETED` 경기만 outbox row를 만든다. |
| `claimed_by`, `claimed_at` | `SKIP LOCKED`로 중복 선점을 막고, 처리 instance는 로그/tracing에서 확인한다. |

outbox row는 `COMPLETED` 경기 데이터에 대해서만 생성된다. `CANCELED` 경기는 랭킹 갱신 대상이 아니므로 outbox row를 만들지 않는다.

## 6. 이벤트 저장 흐름

`KboScoreboardService.updateFromScoreboard()` 메서드에서는 기존의 Spring 이벤트 발행 대신 outbox에 저장하는 과정을 거칩니다.

```java
@Transactional
public void updateFromScoreboard(String gameCode, KboScoreboardGame data) {
    bronzeGameService.upsertByNaturalKey(...);
    GameState state = GameState.fromName(data.getStatus());
    if (state == GameState.COMPLETED) {
        outboxEventService.saveGameCompletedEvent(...);
        reviewRetryQueueService.enqueue(gameCode, Duration.ofMinutes(30));
    }
}
```

`outbox_events`와 `review_crawl_retries` 두 테이블 모두 경기 상태가 `COMPLETED`일 때 insert가 일어나기 때문에, 하나의 조건문에서 처리합니다. `saveGameCompletedEvent()`는 같은 트랜잭션 내에서 `outbox_events`에 상태가 PENDING인 row를 새로 추가합니다.

outbox 중복 저장은 예외를 catch해서 무시하지 않는다. `event_type + aggregate_id`를 멱등 키로 사용하고, DB의 `INSERT IGNORE` 또는 `ON DUPLICATE KEY UPDATE id = id` 같은 멱등 insert로 중복을 정상 흐름으로 처리한다. JPA `save()` 중 unique violation이 발생하면 트랜잭션이 rollback-only가 될 수 있으므로 피한다.

```
COMPLETED
-> outbox_events에 insert
-> review_crawl_retries에 insert (직접 insert, outbox 방식 아님, §8.3)

CANCELED
-> 아무 처리도 하지 않음 (GAME_ETL 관련 처리는 GameEtlScheduler에서 담당, §8.1)
```

## **7. Worker 처리 흐름**

`app` 내부의 Worker가 `outbox_events` 테이블을 주기적으로 조회하여 아직 처리되지 않은 이벤트를 처리한다.

Worker는 한 번에 일정 개수의 이벤트를 가져오며, 처리 대상은 `status`가 `PENDING`이고 `next_retry_at`이 현재 시각 이전인 이벤트다.

```text
PENDING 이벤트 조회
→ 처리할 이벤트 선점
→ payload 역직렬화
→ 후속 작업 수행
→ 성공 시 PROCESSED
→ 실패 시 재시도 정보 갱신 후 PENDING
```

여러 Worker가 동시에 실행되더라도 같은 이벤트를 가져가지 않도록 `SELECT FOR UPDATE SKIP LOCKED`를 사용한다.

```sql
SELECT *
FROM outbox_events
WHERE status = 'PENDING'
  AND next_retry_at <= NOW(6)
ORDER BY id
LIMIT ?
FOR UPDATE SKIP LOCKED;
```

`FOR UPDATE`는 조회한 이벤트에 Lock을 설정하고, `SKIP LOCKED`는 다른 Worker가 이미 선점한 이벤트를 기다리지 않고 건너뛰게 한다.

다만 실제 후속 작업이 끝날 때까지 Lock을 유지하면 DB 트랜잭션이 길어질 수 있다. 따라서 claim 트랜잭션에서는 조회와 `PROCESSING` 변경까지만 수행하고 즉시 커밋한다. 실제 랭킹 갱신은 해당 트랜잭션 밖에서 실행한다.

```text
PENDING
→ PROCESSING
→ PROCESSED
```

처리에 실패하면 `retry_count`를 증가시키고 `next_retry_at`을 갱신한 뒤 다시 `PENDING` 상태로 변경한다.

현재 outbox를 처리하는 논리적 consumer는 app의 `GAME_COMPLETED 후속 처리 worker` 하나다. 다만 Blue-Green 배포 중에는 여러 app 인스턴스가 동시에 polling할 수 있으므로 `FOR UPDATE SKIP LOCKED`를 사용한다. `claimed_by`, `claimed_at`은 두지 않으며, 어떤 인스턴스가 처리했는지는 애플리케이션 로그와 tracing으로 확인한다.

만약 처리 도중 서버가 중단된다면 PROCESSING 상태로 남아 있는 row가 생길 수 있습니다. 이 때는 일정 시간 이상 갱신되지 않은 PROCESSING 상태의 데이터를 recovery job이 다시 PENDING으로 돌려놓아야 합니다.

```
PROCESSING 상태이고 updated_at이 now - timeout보다 이전인 경우
-> retry_count + 1
-> max retry 미만이면 PENDING으로 복구
-> max retry 이상이면 FAILED
```

## 8. 처리 로직

### 8.1 GAME_ETL: outbox를 쓰지 않음

`GameEtlScheduler`가 1분마다 bronze에서 silver로 전체 스캔을 수행하므로 별도 outbox 트리거를 두지 않는다. outbox를 추가해도 얻는 것은 최대 1분의 지연 감소뿐이고, 현재 규모에서는 consumer 추가 비용이 더 크다.

### 8.2 App 후속 처리

`GAME_COMPLETED`는 app에서 다음 후속 작업을 유발한다.

| 후속 작업 | 사용자 기대 | 적정 반영 속도 | 처리 방침 |
| --- | --- | --- | --- |
| 승부 예측 점수 정산 | 경기 종료 직후 내 예측 적중 여부와 점수를 확인 | 수 초~수십 초 | outbox worker의 우선 처리 대상 |
| 주간 승부 예측 랭킹 | 점수 반영 직후 주간 순위 확인 | 수 초~수십 초 | 승부 예측 정산과 함께 처리 |
| 승리요정 랭킹 | 누적 직관 기록 기반 통계 확인 | 수십 초~수분 허용 | 계산 비용이 작으면 함께 처리, 무거우면 별도 polling 또는 기존 scheduler 유지 |

```text
outbox_events row (payload: gameCode, date)
→ prediction score settlement
→ weekly prediction ranking update
→ optional victory fairy ranking update
→ PROCESSED
```

`GameCompletedEvent`는 `COMPLETED` 상태일 때만 outbox에 저장되므로(§6), worker는 별도로 상태 분기를 할 필요 없이 row가 존재하면 경기 완료 후속 처리를 실행하면 된다.

승부 예측 정산과 주간 랭킹은 사용자가 경기 전에 직접 참여한 결과라서 경기 종료 직후 확인하려는 동기가 크다. 반면 승리요정 랭킹은 누적 통계 성격이 강해 1분 정도 늦어도 기능 가치가 크게 떨어지지 않는다.

따라서 처리 비용이 작고 같은 데이터 흐름이라면 함께 즉시 갱신하고, 승리요정 랭킹 계산이 무겁다면 승부 예측 정산과 분리한다.

랭킹 갱신은 중복 실행돼도 같은 결과가 나와야 하므로, 매번 재계산하는 방식이 맞다. 순차 재시도에 대해서는 멱등성을 기대할 수 있다.

다만 멱등성과 동시 실행 안전성은 다르다. 같은 날짜에 여러 경기가 거의 동시에 끝나거나 Blue-Green 배포로 worker 인스턴스가 둘 이상 실행되면 같은 `date`의 `updateRankings(date)`가 동시에 실행될 수 있다. 따라서 구현에서는 다음 규칙을 둔다.

```text
동일한 date에 대한 랭킹 갱신은 동시에 실행하지 않는다.
```

구현 방식은 date 기준 DB lock, 분산 lock, 또는 실제 worker를 하나만 활성화하는 방식 중 하나로 선택한다. 현재 규모에서는 app worker를 하나만 활성화하거나 date 단위 lock을 두는 방식이 가장 단순하다.

### 8.3 REVIEW_CRAWL_QUEUE : outbox를 쓰지 않음

기존 구조는 `AFTER_COMMIT` 리스너(`ReviewCrawlingEventHandler`)에서 `reviewRetryQueueService.enqueue(gameCode, 30분)`를 호출한다. 이 방식은 `bronze_games_raw` commit 이후 프로세스가 죽으면 enqueue가 유실될 수 있다.

따라서 `review_crawl_retries` insert를 `KboScoreboardService.updateFromScoreboard()` 원본 트랜잭션 안으로 옮긴다. `review_crawl_retries` 자체가 상태와 재시도 시각을 가진 영속 작업 큐이므로, 별도의 outbox 이벤트를 한 번 더 만들 필요가 없다.

```text
@Transactional  
  1. bronze_games_raw upsert  
  2. (COMPLETED) outbox_events insert  
  3. (COMPLETED) review_crawl_retries insert   <- outbox 거치지 않음  
commit
```

```text
Before: GameFinalizedEvent -> AFTER_COMMIT 리스너(ReviewCrawlingEventHandler) -> enqueue()
After:  KboScoreboardService.updateFromScoreboard() 트랜잭션 안에서 enqueue() 직접 호출
```

`ReviewRetryScheduler`의 polling/재시도 방식은 그대로 둔다. 중복 방지는 같은 경기의 작업이 여러 상태로 중복 생성되지 않도록 `game_code` 자체를 unique로 둔다.

```sql
UNIQUE KEY uk_review_retry_game (game_code)
```

`(game_code, status)` unique는 기존 row가 `PROCESSING`일 때 새 `PENDING` row가 들어갈 수 있어 중복 방지로 부족하다.

## 9. Payload 계약

Outbox payload는 Spring Event record와 분리한다. DB에 저장되는 메시지 계약은 코드 리팩터링과 독립적으로 유지되어야 한다.

## 10. 실패 처리

현재 구조에서는 Worker가 하나의 `GAME_COMPLETED` 메시지 계약만 처리하므로, `outbox_events`의 한 row가 이벤트 원본이자 처리 상태를 함께 관리한다. 별도의 소비 상태 테이블은 두지 않는다.

### **처리 상태**

| status | 의미 |
| --- | --- |
| `PENDING` | 처리 대기 또는 재시도 대기 |
| `PROCESSING` | worker가 선점하여 처리 중 |
| `PROCESSED` | 처리 성공 |
| `FAILED` | 최대 재시도 초과 |

### **재시도 정책**

처음에는 단순한 정책으로 시작하고, 운영하면서 조정한다.

| 항목 | 의미 | 값 | 값 선정 이유 |
| --- | --- | --- | --- |
| worker polling interval | `PENDING` 이벤트를 얼마나 자주 가져올 것인가 | 1분 | 현재는 승부 예측 정산 기능이 없고 승리요정 랭킹 반영만 필요하므로 1분을 기본값으로 둔다. 승부 예측 정산이 추가되어 더 빠른 반영이 필요해지면 30초 polling 또는 wake-up signal을 다시 검토한다. |
| batch size | 한 번에 선점할 outbox 이벤트 수 | 20 | 현재 이벤트 발생량이 적어 큰 batch가 필요 없다. 여러 경기가 동시에 종료되어도 한 번에 처리 가능한 수준으로 둔다. |
| retry interval | 실패한 이벤트를 언제 다시 처리할 것인가 | 1분 → 5분 → 30분 단계적 증가 | 일시 오류는 빠르게 재시도하되, 반복 실패 시 DB와 후속 작업 부하를 줄인다. |
| max retry | 자동 재시도 한계 | 5회 | 계속 실패하는 이벤트가 무한히 반복되지 않도록 `FAILED`로 격리한다. |
| PROCESSING timeout | 얼마 동안 `PROCESSING`이면 stuck으로 볼 것인가 | 30분 | 랭킹 갱신이나 DB 지연이 일시적으로 길어지는 상황을 정상 처리로 볼 여지를 둔다. |
| recovery polling interval | stuck 여부를 얼마나 자주 검사할 것인가 | 10분 | recovery는 빠른 반영 경로가 아니라 영구 stuck 방지용 안전장치이므로 1분마다 실행할 필요가 없다. |

이벤트 처리에 실패하면 `retry_count`를 먼저 증가시킨다. `retry_count >= 5`이면 `FAILED`로 변경하고, 그 외에는 `next_retry_at`을 갱신한 뒤 `PENDING`으로 되돌린다. Worker는 `next_retry_at`이 현재 시각 이전인 이벤트만 다시 가져와 처리한다.

| 값 | 너무 작을 때 | 너무 클 때 |
| --- | --- | --- |
| worker polling interval | DB polling이 불필요하게 잦아진다. | 경기 완료 후 후속 처리 반영이 늦어진다. |
| PROCESSING timeout | 정상 처리 중인 이벤트를 stuck으로 오판할 수 있다. | 실제 stuck 이벤트가 오래 방치된다. |
| recovery polling interval | stuck 여부 확인 쿼리가 불필요하게 자주 실행된다. | timeout이 지난 이벤트를 늦게 발견한다. |

예를 들어 `PROCESSING timeout`이 30분이고 `recovery polling interval`이 10분이면, 실제 복구는 30분을 넘은 뒤 다음 recovery 실행 시점에 일어난다. 최악의 경우 약 40분 뒤에 복구될 수 있다.

### **PROCESSING 상태 복구**

Worker가 `PROCESSING` 상태에서 비정상 종료되면 해당 이벤트는 다시 가져가지 못한다. 이를 방지하기 위해 `PROCESSING` 상태가 timeout보다 오래 유지된 이벤트는 처리 실패로 보고 `retry_count`를 증가시킨다. 증가 후 `retry_count >= 5`이면 `FAILED`, 그 외에는 `PENDING`으로 복구한다.

`FAILED` 상태의 이벤트는 운영자가 원인을 확인한 뒤 `retry_count`를 초기화하고 상태를 `PENDING`으로 변경하여 다시 처리할 수 있다.


## 11. 운영 쿼리

미처리 이벤트 수:

```sql  
SELECT status, COUNT(*)  
FROM outbox_events  
GROUP BY status;  
```  

오래된 대기 이벤트:

```sql  
SELECT *  
FROM outbox_events  
WHERE status = 'PENDING'  
  AND next_retry_at < DATE_SUB(NOW(6), INTERVAL 5 MINUTE)
ORDER BY next_retry_at;  
```  

실패 이벤트:

```sql  
SELECT event_type, aggregate_id, retry_count, last_error  
FROM outbox_events  
WHERE status = 'FAILED'  
ORDER BY updated_at DESC;  
```  

## 12. 단계별 적용 계획

### 1단계: 저장 구조 추가

- `outbox_events`에 status, retry_count, next_retry_at을 포함한 단일 테이블로 migration을 추가합니다.
- outbox의 entity, repository, service를 만듭니다.
- `GAME_COMPLETED` payload 저장 기능을 구현합니다.

### 2단계: 경기 종료 감지 흐름 변경

- `KboScoreboardService.updateFromScoreboard()`에서 Spring local event 발행은 제거하거나, 필요하면 단순 로그 용도로만 남깁니다.
- `bronze_games_raw`의 upsert, `outbox_events` insert, `review_crawl_retries` insert 작업을 모두 같은 트랜잭션에서 처리하며, 두 조건이 모두 `COMPLETED`일 때만 실행합니다(§6, §8.3 참고).
- outbox 중복 저장은 `event_type + aggregate_id` 멱등 키와 DB의 `INSERT IGNORE` 또는 `ON DUPLICATE KEY UPDATE id = id`로 처리합니다.
- `review_crawl_retries`는 `game_code` unique 제약을 두고, 기존 row가 있으면 새 row를 만들지 않거나 필요한 경우 기존 row를 다시 `PENDING`으로 갱신합니다.

### 3단계: consumer 구현

- `app` worker를 추가합니다. 이 worker는 승부 예측 점수 정산과 주간 승부 예측 랭킹 갱신을 우선 처리하고, 필요하면 승리요정 랭킹도 함께 갱신합니다(§8.2).
- `GAME_ETL`은 outbox로 옮기지 않고, 기존 `GameEtlScheduler`를 그대로 사용합니다(§8.1). 따라서 `GameFinalizedEventHandler`는 더 이상 필요 없어 삭제 대상입니다.
- `REVIEW_CRAWL_QUEUE`는 별도 worker를 추가하지 않습니다. 기존 `ReviewRetryScheduler`가 polling과 재시도를 계속 담당합니다(§8.3). 그리고 `ReviewCrawlingEventHandler`(AFTER_COMMIT 리스너)는 제거합니다.
- `StatScheduler`가 사용하는 기존 Spring local event handler도 함께 제거하거나 더 이상 등록되지 않도록 정리합니다.
- 동일한 `date`의 랭킹 갱신이 동시에 실행되지 않도록 worker 단일 실행 보장 또는 date 단위 lock을 적용합니다.

### 4단계: 운영 보강

- stuck 상태의 `PROCESSING` 이벤트를 복구하는 job을 추가합니다.
- 실패한 이벤트를 재처리할 수 있는 API나 admin 기능을 마련합니다.
- outbox 처리 지연, 실패 횟수, 재시도 횟수에 대한 metric도 추가합니다.

## 13. 최종 결정

- `COMPLETED` 경기만 `GAME_COMPLETED` outbox row로 저장한다. `CANCELED`는 랭킹 갱신 대상이 아니므로 outbox에 저장하지 않는다.
- `event_type`은 Java 클래스명이 아니라 메시지 계약 값인 `GAME_COMPLETED`를 사용한다.
- `bronze_games_raw` 저장, `outbox_events` 멱등 insert, `review_crawl_retries` insert는 같은 DB 트랜잭션에서 처리한다.
- outbox의 논리적 consumer는 app의 `GAME_COMPLETED 후속 처리 worker` 하나다. 실행 worker instance는 Blue-Green 배포 중 여러 개일 수 있으므로 `FOR UPDATE SKIP LOCKED`를 사용한다.
- 이 worker의 우선 처리 대상은 승부 예측 점수 정산과 주간 승부 예측 랭킹 갱신이다. 승리요정 랭킹은 비용이 작으면 함께 처리하고, 무거우면 별도 polling 또는 기존 scheduler로 분리한다.
- worker는 polling만 사용한다. wake-up signal은 현재 단계에서 제거하고, 실제 polling 지연 문제가 관측될 때 별도 설계로 검토한다.
- `GAME_ETL`은 outbox를 사용하지 않고 기존 `GameEtlScheduler`가 1분마다 전체 스캔으로 처리한다.
- `REVIEW_CRAWL_QUEUE`는 outbox를 사용하지 않는다. `review_crawl_retries` 자체가 영속 작업 큐이므로 원본 트랜잭션에서 직접 insert한다.
- 동일한 `date`의 랭킹 갱신은 동시에 실행하지 않는다.
