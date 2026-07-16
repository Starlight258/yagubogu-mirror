# 경기 종료 이벤트 전달 문제 해결 정리

## 1. 문제 정의

현재 야구보구 백엔드는 `app`과 `crawling` 두 개의 Spring Boot 애플리케이션으로 나뉘어 있다.

```text
app/YaguboguApplication
- 클라이언트 API 요청 처리
- 경기, 체크인, 통계, SSE, 회원, 인증 기능 제공
- shared MySQL 조회/수정

crawling/CrawlingApplication
- KBO 스코어보드/게임센터 크롤링
- 원본 데이터를 bronze_games_raw에 저장
- Bronze -> Silver ETL 일부 실행
```

두 애플리케이션은 MySQL을 공유한다. 따라서 DB에 기록된 데이터는 서로 볼 수 있다.

하지만 Spring `ApplicationEventPublisher`는 JVM 내부 이벤트다.

```text
crawling JVM에서 GameFinalizedEvent 발행
-> 같은 crawling JVM 안의 리스너만 수신
-> 별도 app JVM은 수신하지 못함
```

즉 현재 문제는 단순히 “이벤트를 발행하지 않는다”가 아니다.

해결해야 하는 문제는 다음과 같다.

> 경기 종료라는 중요한 도메인 이벤트를 분산 배포 환경에서도 유실/중복 없이 후속 작업으로 연결할 수 있어야 한다.

사용자와 서비스 관점에서는 다음 문제가 발생할 수 있다.

- 경기 종료 후 최신 점수/결과가 API에 늦게 반영될 수 있다.
- 승리 요정 랭킹, 리뷰 크롤링, SSE 알림 같은 후속 작업이 실행 위치에 따라 누락될 수 있다.
- crawling 인스턴스가 여러 대가 되면 같은 경기 종료를 중복 처리할 수 있다.
- 장애가 나면 Spring 로컬 이벤트는 복구할 수 없다.

### 왜 경기 종료를 빠르게 알아야 하는가

경기 종료 이벤트는 단순한 내부 배치 트리거가 아니라, 여러 사용자 기능의 기준점이 된다.

현재 백엔드 코드 기준으로 확인되는 관련 기능은 다음이다.

| 현재 확인된 기능 | 근거 | 경기 종료 반영이 필요한 이유 |
| --- | --- | --- |
| 날짜별 경기 목록 조회 | `GameController`, `GameService.findGamesByDate` | 오늘 경기 카드에서 종료 상태와 스코어를 보여준다. |
| 경기 스코어보드 조회 | `GameService.findScoreBoard` | 사용자가 종료 직후 최종 스코어와 투수/이닝 정보를 확인한다. |
| 체크인 히스토리 | `CheckInService.findCheckInHistory` | 직관한 경기의 승/패 결과가 기록과 연결된다. |
| 체크인 리뷰 조회 | `CheckInService.findCheckInReview` | 경기 종료 후 크롤링된 타자/투수 기록을 보여준다. |
| 승리 요정 랭킹 | `StatService.findVictoryFairyRankings`, `VictoryFairyRankingRepository` | 경기 결과가 랭킹 점수의 기준이 된다. |
| SSE 스트림 | `StreamController`, `SseEventHandler`, `SseEmitterRegistry` | 체크인 팬 비율 등 실시간 화면을 연결된 클라이언트에게 전달한다. |

즉 경기 종료 반영이 늦어지면 사용자는 다음과 같은 불일치를 경험한다.

```text
실제 KBO 경기는 종료됨
-> 앱에서는 아직 LIVE 또는 경기 중 상태
-> 체크인 기록/랭킹/리뷰가 최신 결과와 맞지 않음
```

추가로, 현재 main 코드에는 아직 없지만 PR로 올라간 예정 기능이 있다.

| PR 반영 예정 기능 | 경기 종료 이벤트 전달과의 연결 |
| --- | --- |
| 모바일 위젯 실시간 크롤링 | 사용자가 앱을 열지 않아도 위젯에서 경기 상태와 최종 스코어를 빠르게 확인한다. |

위젯이 반영되면 경기 종료 이벤트의 중요도는 올라간다. 앱 내부 화면은 사용자가 새로고침하거나 재진입하면서 최신 데이터를 확인할 수 있지만, 위젯은 백그라운드에서 제한된 주기로 갱신되기 때문에 서버 쪽 상태 반영이 늦으면 사용자는 앱 밖에서도 오래된 경기 상태를 보게 된다.

즉 현재 코드 기준의 직접 근거는 **경기 목록/스코어보드, 체크인 기록/리뷰, 승리 요정 랭킹, SSE 기반 실시간 데이터**다. PR의 위젯 실시간 크롤링이 반영되면 **모바일 위젯의 최신 상태 제공**도 빠른 경기 종료 반영의 직접 근거가 된다.

반면 다음 기능은 현재 코드와 언급된 PR 기준으로도 확인하지 못했다. 실제 요구사항으로 추가할 때만 빠른 경기 종료 반영의 근거로 사용할 수 있다.

| 아직 확인되지 않은 기능 | 요구사항으로 추가될 경우 의미 |
| --- | --- |
| 푸시 알림 | 경기 종료/승리 결과를 사용자에게 즉시 알려야 한다. |
| 경기 종료 전용 실시간 화면 | 종료 상태 변경을 여러 app 인스턴스의 클라이언트에게 즉시 fan-out해야 한다. |

다만 모든 기능이 같은 수준의 실시간성을 요구하지는 않는다.

| 요구 수준 | 해당 기능 | 허용 가능한 처리 방식 |
| --- | --- | --- |
| 빠른 반영 필요 | 경기 목록/스코어보드, SSE 실시간 데이터, 모바일 위젯(PR 반영 예정) | 이벤트 기반 처리 또는 짧은 polling |
| 수 분 지연 허용 | 리뷰 크롤링, 승리 요정 랭킹 갱신 | Game Server Transactional Outbox + consumer polling |
| 일 단위 지연 허용 | 정산성 랭킹 보정, 데이터 복구성 재계산 | 일일 배치/주기적 재계산 |
| 요구사항 추가 시 빠른 반영 필요 | 푸시 알림 | 이벤트 기반 처리 + app fan-out |

따라서 이 문제의 목표는 모든 후속 작업을 무조건 실시간으로 만드는 것이 아니다.

핵심은 다음과 같다.

> 빠른 반영이 필요한 사용자-facing 상태는 지연을 줄이고, 정합성이 중요한 후속 작업은 유실/중복 없이 처리되도록 분리한다.

이 문제는 토스뱅크 Server Developer 역량 기준으로 보면 다음 주제와 직접 연결된다.

| 역량 | 이 문제와의 연결 |
| --- | --- |
| 문제 해결 | 현상을 “이벤트가 안 온다”로 끝내지 않고 배포 구조와 데이터 흐름까지 정의해야 한다. |
| 안정성 | 경기 종료 후속 작업이 장애나 재시작에도 복구 가능해야 한다. |
| 데이터 정합성 | 같은 경기 결과가 중복 반영되거나 유실되면 안 된다. |
| 성능 | 트래픽이 늘어도 DB polling, SSE fan-out, worker 처리량이 병목이 되면 안 된다. |
| 운영 | 실패 이벤트 관측, 재처리, 정리 정책이 있어야 한다. |
| 확장성 | app/crawling 인스턴스를 늘려도 구조가 깨지지 않아야 한다. |

## 2. 성공 기준

문제가 해결되었다고 판단할 기준을 먼저 정한다.

| 항목 | 현재 상태 | 목표 |
| --- | --- | --- |
| 인스턴스 간 전달 | Spring 이벤트는 JVM 내부에서만 전달 | crawling에서 감지한 경기 종료를 app/worker가 처리 가능 |
| 이벤트 유실 | JVM 장애 시 이벤트 복구 불가 | 이벤트가 영속화되어 재처리 가능 |
| 중복 처리 | 여러 crawling 인스턴스가 같은 경기 종료 감지 가능 | 같은 `gameCode`는 한 번만 후속 처리 |
| 처리 지연 | 주기적 ETL에 의존 | 요구사항에 맞는 지연 시간 내 처리 |
| 운영 관측 | 이벤트 처리 상태 추적 어려움 | 미처리/처리/실패 이벤트 조회 가능 |
| 확장성 | app/crawling 분리 배포 시 이벤트 연결 불명확 | 인스턴스 수와 무관하게 일관된 처리 |

정량 지표는 테스트에서 다음처럼 확인한다.

| 지표 | 측정 목적 |
| --- | --- |
| 이벤트 생성부터 처리 완료까지 p95/p99 지연 | 사용자가 최신 상태를 언제 보게 되는지 확인 |
| 중복 처리 횟수 | 데이터 정합성 확인 |
| 미처리 이벤트 수 | 장애/병목 관측 |
| DB lock wait/deadlock | DB Outbox 방식의 병목 확인 |
| SSE 수신 누락률 | 실시간 알림 구조 검증 |

## 3. 병목 범위 식별

경기 종료 시 현재 흐름은 다음과 같다.

```text
AdaptivePoller
-> KBO 스코어보드 조회
-> Game 상태 변화 감지
-> KboScoreboardService.updateFromScoreboard
-> bronze_games_raw upsert
-> COMPLETED/CANCELED이면 Spring GameFinalizedEvent 발행
-> 종료된 경기는 polling schedule에서 제거
```

ETL은 별도 경로로 실행된다.

```text
bronze_games_raw
-> GameEtlService.transformBronzeToSilver(...)
-> games
```

현재 ETL 실행 경로는 세 가지다.

- `GameEtlScheduler`: 1분마다 최근 Bronze 데이터를 Silver로 변환
- `GameScheduler`: 매일 초기 크롤링 이후 즉시 ETL 실행
- `GameFinalizedEventHandler`: 로컬 `GameFinalizedEvent`를 받으면 단일 경기 즉시 ETL 실행

병목 또는 불안정성이 생길 수 있는 범위는 다음과 같다.

```text
KBO API
  ↓
crawling poller
  ↓
bronze_games_raw 저장
  ↓
Spring local event
  ↓
ETL / ranking / review / SSE
  ↓
app API response
```

이 중 이번 문제의 핵심 범위는 `Spring local event -> 후속 작업` 구간이다.

DB 저장은 공유되지만, Spring 이벤트는 공유되지 않는다. 따라서 분산 환경에서 후속 작업의 트리거로 Spring 이벤트만 쓰는 것이 병목이자 장애 지점이다.

## 4. 가설 수립 및 측정

### 가설 1. Spring 이벤트는 app 인스턴스로 전달되지 않는다

근거:

- `ApplicationEventPublisher`는 같은 Spring ApplicationContext 안에서 동작한다.
- `crawling`과 `app`은 별도 Spring Boot 애플리케이션이다.
- 따라서 `crawling`에서 발행한 이벤트는 별도 `app` JVM으로 전달되지 않는다.

검증 방법:

- crawling에서 `GameFinalizedEvent` 발행 로그 확인
- app 서버의 `GameFinalizedEventHandler` 실행 로그 확인
- 별도 프로세스로 띄웠을 때 app 쪽 로그가 찍히지 않으면 가설 성립

### 가설 2. 여러 crawling 인스턴스가 있으면 같은 경기를 중복 감지할 수 있다

근거:

- Adaptive Poller는 각 crawling 인스턴스에서 독립적으로 실행될 수 있다.
- 같은 KBO 스코어보드를 보고 같은 상태 변화를 감지할 수 있다.

검증 방법:

- crawling 2대를 띄우고 같은 경기 종료 데이터를 입력
- `GameFinalizedEvent` 또는 outbox row 생성 횟수 확인
- 후속 랭킹/ETL 처리 횟수 확인

### 가설 3. 이벤트 처리량보다 후속 DB 작업이 병목일 수 있다

근거:

- 경기 종료 이벤트 자체는 하루 경기 수 수준이라 많지 않다.
- 실제 비용은 ETL, 랭킹 재계산, SSE fan-out에서 발생한다.

검증 방법:

- 이벤트 생성량과 후속 처리 시간을 분리 측정
- DB query time, lock wait, worker 처리 시간을 각각 확인

## 5. 원인 분석

근본 원인은 다음 세 가지다.

### 원인 1. 이벤트 전달 매체가 JVM 내부로 한정되어 있다

Spring 이벤트는 같은 JVM 안의 객체 간 decoupling에는 적합하지만, 서버 간 통신 수단이 아니다.

```text
같은 JVM 내부 callback: 적합
서버 간 이벤트 전달: 부적합
장애 복구가 필요한 이벤트 로그: 부적합
```

### 원인 2. 경기 종료 이벤트에 대한 durable source of truth가 없다

현재 경기 종료 사실은 다음 중 하나로만 존재한다.

- KBO 응답
- `bronze_games_raw`의 payload/state
- JVM 내부 Spring 이벤트
- `games`에 ETL된 결과

하지만 “이 경기 종료 이벤트가 후속 작업으로 처리되었는가?”를 추적하는 별도 상태가 없다.

그래서 다음 질문에 답하기 어렵다.

- 어떤 경기 종료 이벤트가 미처리 상태인가?
- 어떤 이벤트가 처리 중 실패했는가?
- 같은 이벤트가 중복 처리되었는가?
- 재처리가 필요한 이벤트는 무엇인가?

### 원인 3. 후속 작업별 요구사항이 다르다

모든 후속 작업을 같은 이벤트 방식으로 처리하면 trade-off가 흐려진다.

| 후속 작업 | 요구사항 |
| --- | --- |
| Bronze -> Silver ETL | 유실되면 안 됨, 중복 처리 방지 필요 |
| 승리 요정 랭킹 | 정합성 중요, 중복 반영 금지 |
| 리뷰 크롤링 큐 | 재시도/지연 처리 필요 |
| SSE 알림 | 실시간성 중요, 연결된 app 인스턴스별 fan-out 필요 |
| 모바일 위젯(PR 반영 예정) | 앱 외부에서 최신 경기 상태를 보여주므로 서버 상태 반영 지연이 사용자에게 바로 노출됨 |

따라서 핵심 이벤트 처리와 실시간 알림을 분리해서 설계해야 한다.

## 6. 대안 비교

### 선택지 A. Spring 이벤트만 유지

```text
crawling JVM
-> ApplicationEventPublisher
-> 같은 JVM 리스너만 실행
```

장점:

- 새 인프라가 필요 없다.
- 코드 변경이 거의 없다.
- 로컬 개발이 쉽다.

단점:

- 인스턴스 간 전달이 안 된다.
- 이벤트 유실 복구가 안 된다.
- 여러 crawling 인스턴스에서 중복 처리 가능성이 있다.
- app 서버의 즉시 반응을 보장할 수 없다.

적합한 상황:

- 단일 JVM 배포
- 후속 작업 유실이 중요하지 않고 배치 복구가 충분한 경우

판단:

- 분산 배포 문제를 해결하지 못한다.

### 선택지 B. DB Outbox / 이벤트 테이블

경기 종료 사실을 MySQL에 이벤트 row로 남긴다.

```text
crawling
-> game_finalized_events insert

app 또는 worker
-> 미처리 이벤트 polling
-> DB lock으로 claim
-> 후속 작업 처리
-> processed_at 기록
```

예시 테이블:

```sql
CREATE TABLE game_finalized_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_code VARCHAR(255) NOT NULL,
    game_date DATE NOT NULL,
    stadium VARCHAR(50) NOT NULL,
    home_team VARCHAR(50) NOT NULL,
    away_team VARCHAR(50) NOT NULL,
    start_time TIME NULL,
    state VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    processed_at DATETIME(6) NULL,
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT NULL,
    UNIQUE KEY uq_game_finalized_events_game_code (game_code)
);
```

여러 processor가 동시에 처리한다면 다음 방식으로 claim한다.

```sql
SELECT *
FROM game_finalized_events
WHERE processed_at IS NULL
ORDER BY id
LIMIT 100
FOR UPDATE SKIP LOCKED;
```

장점:

- 기존 MySQL만으로 구현 가능하다.
- 이벤트가 영속화되어 장애 후 복구할 수 있다.
- `UNIQUE(game_code)`로 중복 감지를 DB 레벨에서 막을 수 있다.
- 운영자가 미처리/실패 이벤트를 직접 조회할 수 있다.
- app/crawling이 분리되어도 동작한다.

단점:

- polling 주기만큼 지연이 생긴다.
- DB lock과 트랜잭션 설계를 신경 써야 한다.
- 이벤트 테이블 정리 정책이 필요하다.
- 이벤트량이 매우 많아지면 DB 부하가 될 수 있다.

적합한 상황:

- 경기 종료처럼 이벤트량은 낮지만 유실되면 안 되는 이벤트
- 새 인프라 없이 안정성과 정합성을 확보해야 하는 경우

판단:

- 현재 문제의 1차 해결책으로 가장 적합하다.

### 선택지 C. Redis Pub/Sub

Redis 채널로 이벤트를 발행하고 app 서버들이 구독한다.

```text
crawling
-> Redis publish
-> app servers subscribe
-> 각 app 서버가 자기 SSE 클라이언트에게 전송
```

장점:

- 지연이 낮다.
- 여러 app 인스턴스에 fan-out하기 좋다.
- SSE 실시간 알림과 잘 맞는다.

단점:

- 일반 Pub/Sub은 영속성이 없다.
- app 서버가 재시작 중이면 이벤트를 놓칠 수 있다.
- 중복 처리 방지는 별도 DB idempotency가 필요하다.
- Redis 운영 의존성이 생긴다.

적합한 상황:

- SSE fan-out처럼 빠른 전달이 중요하고, 최종 정합성은 DB로 보완 가능한 경우

판단:

- 핵심 경기 종료 처리의 source of truth보다는 실시간 알림 보조 수단으로 적합하다.

### 선택지 D. Redis Stream

Redis Stream과 consumer group을 사용한다.

```text
crawling
-> XADD game-finalized

worker group
-> XREADGROUP
-> 처리
-> XACK
```

장점:

- Pub/Sub보다 유실에 강하다.
- consumer group으로 worker 확장이 쉽다.
- DB polling보다 낮은 지연을 기대할 수 있다.

단점:

- DB Outbox보다 구현/운영 복잡도가 높다.
- pending message 복구 정책이 필요하다.
- Redis 장애와 보존 정책을 고려해야 한다.
- idempotency는 여전히 DB에서 보강해야 한다.

적합한 상황:

- Redis가 이미 운영 인프라에 포함되어 있음
- 초당 수백~수천 건 이벤트를 worker group으로 처리해야 함

판단:

- 중간 규모 이상에서 고려할 만하지만, 현재 경기 종료 이벤트만 보면 DB Outbox보다 무겁다.

### 선택지 E. Kafka / SQS / 메시지 브로커

이벤트 브로커나 관리형 큐를 사용한다.

```text
crawling
-> topic/queue publish
-> app/worker consume
```

장점:

- 서비스 간 이벤트 계약이 명확해진다.
- 내구성, 재시도, DLQ, consumer 확장에 강하다.
- 이벤트 종류가 많아질수록 구조적 장점이 커진다.

단점:

- 인프라와 운영 복잡도가 가장 크다.
- 로컬 개발 환경도 무거워진다.
- 현재 규모에서는 과할 수 있다.

적합한 상황:

- 이벤트 종류가 많고 장기적으로 이벤트 기반 아키텍처를 가져갈 경우
- DLQ, 장기 보관, 재처리, 서비스 간 비동기 통신이 중요해진 경우

판단:

- 장기 확장안으로는 좋지만 지금 당장 도입하기에는 비용이 크다.

### 선택지 F. 단일 crawling/worker 인스턴스 강제

운영상 crawling worker를 정확히 1대만 띄워서 중복 감지를 피한다.

```text
single crawling worker
-> 경기 종료 감지
-> ETL
-> 랭킹 갱신
-> 리뷰 큐 등록
```

장점:

- 구조가 단순하다.
- 새 인프라가 필요 없다.
- 단기 안정화에 빠르다.

단점:

- 확장성이 없다.
- 운영 규칙으로만 중복을 막는다.
- worker 장애 시 처리 지연이 발생한다.
- app 서버 여러 대의 SSE fan-out 문제는 해결하지 못한다.

적합한 상황:

- 단기 운영 제약
- 트래픽이 낮고 crawling을 1대만 운영해도 충분한 경우

판단:

- 임시 제약으로는 가능하지만 장기 해결책은 아니다.

### 선택지 G. 이벤트 전달 없이 1분 ETL만 유지

경기 종료 이벤트를 외부로 전달하지 않고, Bronze -> Silver ETL 스케줄러만 1분 주기로 실행한다.

```text
KBO 스코어보드 크롤링
-> bronze_games_raw 저장
-> 최대 약 1분 뒤 GameEtlScheduler 실행
-> games / score_boards 갱신
-> 사용자가 조회하면 최신 경기 상태 확인
```

이 방식은 경기 상태 조회에는 충분할 수 있다. 경기 목록, 스코어보드, 체크인 히스토리처럼 `games`와 `score_boards`를 직접 조회하는 기능은 ETL 이후 최신 상태를 볼 수 있다.

하지만 현재 코드 기준으로 후속 작업은 자동으로 실행되지 않는다.

| 기능 | 1분 ETL만 있을 때 동작 |
| --- | --- |
| 경기 목록/스코어보드 | ETL 이후 반영된다. 최대 약 1분 지연을 감수한다. |
| 체크인 히스토리 승/패 결과 | 조회가 `games` 상태/점수 기준이면 ETL 이후 반영된다. |
| 승리 요정 랭킹 | 즉시 갱신되지 않는다. 현재는 `GameFinalizedEvent` 또는 매일 03:00 스케줄러가 갱신 트리거다. |
| 리뷰 크롤링 | 새로 예약되지 않는다. 현재는 `GameFinalizedEvent`를 받아 30분 뒤 retry queue에 넣는다. |
| SSE/위젯 최신 상태 | DB polling 방식이면 가능하지만, 서버가 먼저 fan-out하는 실시간 전파는 없다. |

특히 승리 요정 랭킹은 조회 시마다 원천 데이터를 즉석 계산하는 구조가 아니다. `victory_fairy_rankings` 테이블을 미리 갱신해두고, 랭킹 API는 이 테이블을 조회한다.

현재 갱신 경로는 다음 두 가지다.

```text
1. GameFinalizedEvent 수신
   -> statSyncService.updateRankings(event.date())

2. 매일 03:00 스케줄러
   -> statSyncService.updateRankings(yesterday)
```

따라서 이벤트를 제거하면 다음 흐름이 된다.

```text
경기 종료
-> 최대 약 1분 뒤 games 업데이트
-> 경기 조회/체크인 히스토리는 최신화 가능
-> victory_fairy_rankings는 그대로
-> 다음날 03:00 스케줄러 이후 랭킹 반영
```

적합한 상황:

- 사용자에게 경기 결과 조회만 1분 이내로 제공하면 충분한 경우
- 승리 요정 랭킹, 리뷰 크롤링, 알림, 위젯 최신 상태가 즉시 반응하지 않아도 되는 경우
- 후속 작업을 별도 일일 배치 또는 수동 재처리로 복구해도 되는 경우

판단:

- 경기 조회 최신화만 목표라면 가장 단순하다.
- 하지만 “경기 종료를 계기로 다른 작업을 시작한다”는 요구사항에는 부족하다.
- 승리 요정 랭킹과 리뷰 크롤링을 현재 UX에서 빠르게 반영하려면 별도 이벤트/큐/스케줄 트리거가 필요하다.

## 7. DDD 기반 Game Server 분리 전제

향후 구조는 DDD 관점에서 경기 도메인을 별도 서버로 분리하는 방향을 전제로 한다.

이 경우 `crawling`과 `games`를 느슨하게 연결하는 현재 구조보다 책임 경계가 명확해진다.

```text
Game Server / Game Bounded Context
- KBO 경기 데이터 수집
- 경기 일정 관리
- 경기 상태 변경
- 스코어보드 저장
- 경기 종료 판정
- GameFinalizedEvent 발행
```

즉 Game Server는 경기 상태의 source of truth가 된다.

```text
KBO API
-> Game Server
-> 경기 상태 저장
-> 경기 종료 판정
-> GameFinalizedEvent 발행
```

이렇게 모으면 Game Server 내부에서는 다음 문제가 줄어든다.

- `crawling` 서버가 이벤트를 발행하고 `app` 서버가 받지 못하는 문제
- Bronze 저장과 Silver 반영 책임이 여러 애플리케이션에 흩어지는 문제
- 경기 상태 변경의 소유자가 불명확한 문제

하지만 Game Server로 합쳐도 이벤트 전달 자체가 사라지는 것은 아니다.

다른 bounded context 또는 서버가 경기 종료에 반응해야 한다면 외부 이벤트는 여전히 필요하다.

```text
Game Server
-> GameFinalizedEvent
-> Stats Server: 승리 요정 랭킹 갱신
-> Review Worker: 리뷰 크롤링 예약
-> Notification/SSE Server: 실시간 알림 또는 위젯 최신 상태 반영
-> CheckIn Context: 체크인 히스토리 결과 반영
```

따라서 설계 방향은 다음처럼 나뉜다.

| 구분 | 해결 방식 |
| --- | --- |
| 경기 데이터 수집/상태 변경 | Game Server 내부 책임으로 통합 |
| 경기 종료 사실의 저장 | Game Server DB 트랜잭션 안에서 저장 |
| 다른 서버로 알림 | Game Server가 도메인 이벤트를 외부로 발행 |
| 유실/중복 방지 | Transactional Outbox 또는 메시지 브로커 + idempotency |

핵심은 **Game Server가 경기 종료 이벤트의 유일한 발행자**가 되는 것이다.

이렇게 하면 “어느 서버가 경기 종료를 판단하는가?”라는 질문에 명확히 답할 수 있다.

```text
경기 종료 판단: Game Server
경기 종료 이벤트 발행: Game Server
이벤트 소비: Stats / Review / Notification / Widget 관련 서버
```

## 8. 해결책 선택

Game Server 분리를 전제로 하면, 1차 해결책은 **Game Server 내부 Transactional Outbox**가 가장 합리적이다.

기존 문서에서 말한 DB Outbox는 “공유 MySQL에 이벤트 테이블을 두자”에 가까웠다. Game Server 분리 후에는 이 개념을 더 명확히 해서, **경기 상태 변경과 이벤트 저장을 같은 Game Server DB 트랜잭션 안에서 처리하는 Transactional Outbox**로 보는 것이 맞다.

```text
Game Server transaction
1. games 상태를 COMPLETED/CANCELED로 변경
2. outbox_events에 GameFinalizedEvent 저장
3. transaction commit

Outbox Relay
-> 미발행 이벤트 조회
-> Redis Stream/Kafka/SQS/Redis PubSub 등으로 발행
-> published_at 기록
```

선택 이유:

1. 데이터 정합성이 가장 중요하다.
   - 경기 종료는 후속 ETL/랭킹/리뷰의 기준 이벤트다.
   - 유실보다 약간의 지연이 낫다.

2. Game Server가 경기 도메인의 source of truth가 된다.
   - 경기 종료 판단과 이벤트 발행 책임이 한 곳으로 모인다.
   - 다른 서버는 Game Server의 도메인 이벤트를 신뢰하면 된다.

3. 이벤트 발생량이 낮다.
   - 경기 종료 이벤트는 하루 경기 수에 비례한다.
   - DB polling이 큰 병목이 될 가능성이 낮다.

4. 중복 방지를 DB 레벨에서 강제할 수 있다.
   - outbox에 `UNIQUE(event_type, aggregate_id)` 또는 `UNIQUE(game_code, state)`를 둘 수 있다.

5. 운영 관측이 쉽다.
   - 미처리/실패 이벤트를 SQL로 바로 확인할 수 있다.
   - 재처리도 쉽다.

반대로 이벤트 전달 없이 1분 ETL만 유지하면 경기 조회 최신화는 단순하게 해결할 수 있다. 하지만 승리 요정 랭킹 갱신, 리뷰 크롤링 예약, SSE/위젯 fan-out처럼 경기 종료를 계기로 시작해야 하는 후속 작업은 별도 트리거가 사라진다. 이 요구사항이 남아 있다면 1분 ETL은 경기 상태 동기화 수단이고, 도메인 이벤트 전달을 대체하지는 못한다.

다만 SSE 실시간 fan-out이나 위젯 최신 상태 제공까지 해결하려면 Outbox만으로는 부족할 수 있다. 이 경우 Transactional Outbox를 durable source로 두고 Redis Pub/Sub 또는 Redis Stream을 보조적으로 붙이는 구조가 적합하다.

```text
1차: Game Server 내부 Transactional Outbox로 안정성과 정합성 확보
2차: Redis Pub/Sub 또는 Redis Stream으로 SSE/위젯용 빠른 상태 전파 최적화
3차: 이벤트량 증가 시 Redis Stream 또는 Kafka/SQS 검토
```

## 9. 구현 설계

### 9.1 Game Server 내부 경기 종료 처리

Game Server에서 KBO 데이터를 수집하고 경기 상태를 변경한다.

```text
KBO scoreboard
-> Game Server
-> 경기 상태 변경
-> games 저장
-> outbox_events에 GameFinalizedEvent 저장
```

경기 상태 변경과 outbox 저장은 같은 트랜잭션 안에서 처리한다.

```text
transaction begin
-> games.game_state = COMPLETED
-> outbox_events insert(GameFinalizedEvent)
transaction commit
```

이렇게 하면 다음 불일치를 막을 수 있다.

```text
DB에는 경기 종료 저장 성공
이벤트 발행 실패
-> 다른 서버는 경기 종료를 모름

이벤트 발행 성공
DB 저장 실패
-> 다른 서버는 존재하지 않는 종료 이벤트를 받음
```

중복 감지는 정상 흐름으로 본다.

```text
insert 성공
-> 처음 감지한 경기 종료

duplicate key
-> 이미 감지된 경기 종료
-> 오류가 아니라 skip
```

### 9.2 Outbox Relay

Outbox Relay는 Game Server DB의 미발행 이벤트를 외부 메시지 채널로 전달한다.

```text
@Scheduled(fixedDelay = ...)
-> 미발행 outbox 이벤트 조회
-> FOR UPDATE SKIP LOCKED로 claim
-> Redis Stream/Kafka/SQS/Redis PubSub 등에 publish
-> published_at 기록
```

Relay는 이벤트를 직접 처리하지 않고 전달만 담당하는 것이 좋다. 실제 후속 작업은 각 bounded context의 consumer가 맡는다.

### 9.3 Consumer / 후속 작업

`GameFinalizedEvent`를 구독하는 쪽은 각자 자신의 책임만 처리한다.

후속 작업 후보:

- 완료 경기라면 승리 요정 랭킹 갱신 또는 랭킹 재계산 트리거
- 리뷰 크롤링 큐 등록
- 필요 시 SSE 알림 이벤트 발행
- PR 반영 후 필요 시 위젯 갱신용 상태 이벤트 발행 또는 위젯 조회 API 최신화

### 9.4 책임 분리

```text
Game Server 책임
- KBO 데이터 수집
- 경기 상태 변경
- 스코어보드 저장
- 경기 종료 판정
- GameFinalizedEvent outbox 저장

Stats 책임
- 승리 요정 랭킹 갱신

Review 책임
- 리뷰 크롤링 예약 및 실행

Realtime/Notification 책임
- SSE 연결 관리
- 연결된 클라이언트에게 fan-out
- PR 반영 후 위젯이 조회하는 최신 경기 상태 제공
```

### 9.5 멱등성

외부 메시지 전달은 at-least-once로 보는 것이 안전하다.

따라서 같은 이벤트가 두 번 처리되어도 결과가 한 번만 반영되어야 한다.

적용 방법:

- `outbox_events`에 aggregate id + event type unique key
- relay claim 시 row lock
- consumer별 `processed_events` 테이블 또는 unique key
- 후속 델타 처리에는 processed table 또는 unique key 추가
- 랭킹은 가능하면 델타 누적보다 절대값 재계산 선호

## 10. 검증 계획

### 10.1 Transactional Outbox 처리량 테스트

목표:

- Game Server의 MySQL outbox relay가 경기 종료 이벤트 전달에 충분한지 확인한다.

시나리오:

```text
1. outbox_events에 미발행 GameFinalizedEvent N개 삽입
2. relay 1대 실행
3. relay 2대, 4대로 늘려 실행
4. FOR UPDATE SKIP LOCKED 적용 여부 비교
```

측정 지표:

- 초당 처리 이벤트 수
- 이벤트 생성부터 `published_at`까지 걸린 시간
- DB CPU
- lock wait
- deadlock 발생 여부
- 중복 처리 여부

### 10.2 중복 경기 종료 감지 테스트

목표:

- Game Server가 같은 KBO 경기 종료 상태를 여러 번 감지해도 같은 경기 종료 이벤트가 한 번만 저장/발행되는지 확인한다.

시나리오:

```text
1. Game Server 또는 경기 수집 worker 2대 실행
2. 같은 경기 종료 상태를 동시에 감지하도록 구성
3. outbox_events insert 결과 확인
4. 후속 ETL/랭킹이 한 번만 반영되는지 확인
```

측정 지표:

- outbox row 수
- duplicate key 발생 수
- relay/consumer 실행 횟수
- `victory_fairy_rankings` 중복 증가 여부

### 10.3 장애 복구 테스트

목표:

- 이벤트 처리 중 서버가 죽어도 복구되는지 확인한다.

시나리오:

```text
1. outbox 이벤트 생성
2. relay가 이벤트 claim 후 publish 전에 강제 종료
3. relay 재시작
4. 이벤트가 다시 발행되거나 실패 상태로 관측되는지 확인
```

측정 지표:

- 미처리 이벤트 잔존 여부
- stuck 이벤트 수
- retry count
- last_error 기록
- 최종 데이터 정합성

### 10.4 SSE/위젯 최신 상태 지연 테스트

목표:

- app 인스턴스가 여러 대일 때 모든 클라이언트에게 알림이 도달하는지 확인한다.
- PR 반영 후 위젯이 경기 종료 상태를 얼마나 빠르게 최신화하는지 확인한다.

시나리오:

```text
1. app 서버 2~3대 실행
2. 각 app 서버에 SSE 클라이언트 다수 연결
3. PR 반영 후 위젯 조회/갱신 요청을 일정 주기로 발생
4. 경기 종료 또는 체크인 이벤트 발생
5. DB polling만 사용했을 때와 Redis Pub/Sub 사용 시 비교
```

측정 지표:

- 이벤트 발행부터 클라이언트 수신까지 p50/p95/p99 지연
- KBO 경기 종료 감지부터 위젯 응답이 종료 상태를 반환하기까지의 지연
- 서버별 SSE 연결 수
- 위젯 조회 API QPS
- 위젯 응답의 stale 비율
- 누락 이벤트 수
- 재연결 횟수
- app 서버 CPU/메모리

### 10.5 Transactional Outbox vs Redis Stream 비교 테스트

목표:

- 이벤트량이 늘어났을 때 Transactional Outbox relay가 병목인지 확인한다.

시나리오:

```text
1. 초당 100, 500, 1000개 이벤트 생성
2. Transactional Outbox relay 처리량 측정
3. Redis Stream consumer group 처리량 측정
4. 같은 idempotency DB write를 포함한 end-to-end 처리량 비교
```

측정 지표:

- end-to-end 처리량
- p95/p99 지연
- DB write QPS
- consumer lag
- broker 처리 지연
- 운영 복잡도

## 11. 트래픽 증가에 따른 구조 선택

트래픽이 많아질수록 이벤트를 한 방식으로 모두 처리하면 안 된다. 이벤트 성격에 따라 분리해야 한다.

### 트래픽 낮음

조건:

- Game Server 1대 또는 경기 수집 worker 1대
- app 인스턴스 1~2대
- 경기 종료 후 수 초~수십 초 지연 허용
- 이벤트 처리량이 낮음

추천:

```text
Game Server 내부 Transactional Outbox
또는 단기적으로 single game worker
```

이유:

- 경기 종료 이벤트 자체는 적다.
- MySQL만으로 충분히 처리 가능하다.
- 운영 복잡도를 낮게 유지할 수 있다.

### 트래픽 중간

조건:

- app 인스턴스 여러 대
- SSE 연결 수 증가
- PR 반영 후 위젯 조회/갱신 요청 증가
- 사용자에게 빠른 상태 반영 필요
- 핵심 이벤트는 유실되면 안 됨

추천:

```text
Transactional Outbox + Redis Pub/Sub
```

역할:

- Transactional Outbox: durable source of truth
- Redis Pub/Sub: 실시간 fan-out

이유:

- Outbox만 쓰면 SSE 실시간성이 떨어질 수 있다.
- Outbox만 쓰면 위젯이 최신 상태를 보는 시점도 polling/ETL 지연에 묶일 수 있다.
- Pub/Sub만 쓰면 유실과 복구 문제가 있다.
- 둘을 조합하면 안정성과 실시간성을 분리할 수 있다.

### 트래픽 높음

조건:

- 이벤트 종류가 많아짐
- worker를 여러 대로 확장해야 함
- retry, DLQ, consumer lag 관측이 필요함
- DB polling이 병목이 됨

추천:

```text
Redis Stream
또는 Kafka/SQS
```

Redis Stream이 적합한 경우:

- Redis를 이미 운영 중
- Kafka까지는 과함
- consumer group 기반 분산 처리가 필요

Kafka/SQS가 적합한 경우:

- 이벤트가 서비스 간 계약이 됨
- 장기 보관, DLQ, 재처리가 중요함
- 이벤트 종류와 처리량이 계속 늘어남

## 12. 이벤트 종류별 권장 구조

| 이벤트 종류 | 발생량 | 유실 허용 | 추천 구조 |
| --- | --- | --- | --- |
| 경기 종료 | 낮음 | 낮음 | Game Server Transactional Outbox |
| 경기 상태 저장/ETL | 낮음/중간 | 낮음 | Game Server 내부 트랜잭션 또는 단일 worker |
| 승리 요정 랭킹 갱신 | 낮음/중간 | 낮음 | GameFinalizedEvent 구독 + 멱등 처리 |
| 체크인 생성 알림 | 중간/높음 | 중간 | DB 반영 + Redis Pub/Sub fan-out |
| SSE 실시간 알림 | 높음 | 일부 허용 | Redis Pub/Sub |
| 모바일 위젯 상태 갱신(PR 반영 예정) | 중간/높음 | 일부 허용, 최종 정합성 필요 | DB 최신화 + Redis Pub/Sub 또는 짧은 polling |
| 좋아요/채팅 실시간 이벤트 | 높음 | 이벤트별 다름 | Redis Stream 또는 Kafka/SQS 고려 |

## 13. Trade-off 및 회고

이번 의사결정의 핵심 trade-off는 다음과 같다.

| 얻는 것 | 잃는 것 |
| --- | --- |
| 이벤트 유실 방지 | outbox relay 지연 발생 |
| 중복 처리 방지 | outbox 테이블/relay/consumer 멱등성 구현 필요 |
| 장애 복구 가능 | 처리 상태 관리 필요 |
| 운영 관측 가능 | 이벤트 정리 정책 필요 |
| 새 인프라 없이 시작 가능 | 매우 높은 이벤트 처리량에는 한계 |

Transactional Outbox는 가장 빠른 구조가 아니라 가장 안전하고 Game Server 분리 구조에 맞는 구조다.

토스식 문제 해결 관점에서 중요한 점은 “Redis/Kafka를 쓰느냐”가 아니라, 다음 질문에 답할 수 있는 것이다.

- 이 문제의 본질은 이벤트 전달인가, 데이터 정합성인가?
- 유실과 중복 중 무엇이 더 치명적인가?
- 현재 서비스 규모에서 어떤 복잡도까지 감당할 수 있는가?
- 트래픽이 증가하면 어떤 지표를 보고 구조를 바꿀 것인가?

현재 답은 다음과 같다.

```text
1. 경기 종료는 유실되면 안 되는 도메인 이벤트다.
2. Game Server 분리 후에는 Transactional Outbox가 안정성/정합성/운영 비용의 균형이 가장 좋다.
3. SSE 또는 위젯 최신 상태 제공이 병목이 되면 Redis Pub/Sub을 보조로 추가한다.
4. 이벤트량과 worker 확장이 본격 문제가 되면 Redis Stream 또는 Kafka/SQS로 확장한다.
```

## 남은 의사결정 질문

- `GameFinalizedEvent`를 `COMPLETED`만 대상으로 볼지, `CANCELED`까지 포함할지?
- 리뷰 크롤링 큐 등록은 Review consumer가 맡을지, Game Server 내부 후속 작업으로 둘지?
- 승리 요정 랭킹은 경기 종료 즉시 갱신해야 하는지, 일일/주기적 재계산으로 충분한지?
- SSE/위젯 즉시 갱신이 필요하다면 DB polling으로 충분한지, Redis Pub/Sub을 추가할지?
- 운영 환경에서 Game Server 내부 경기 수집 worker를 정확히 1대만 둘 수 있는지, 아니면 확장 가능성을 열어둘지?
