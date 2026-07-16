# ⚾️ 야구보구 👀

## 📍 서비스 주제

야구 팬의 활동을 수치화해 성취감과 경쟁심을 자극하고, 직관의 순간을 기록해 팬심을 더욱 뜨겁게 만들어주는 서비스

## 💡 주요 기능

- **경기 정보 조회**: KBO 경기 일정, 경기 상태, 경기 결과를 확인할 수 있습니다.
- **직관 체크인**: 사용자가 관람한 경기를 체크인으로 기록하고, 구장별 방문 경험을 쌓을 수 있습니다.
- **직관 리뷰**: 체크인한 경기의 메모, 이미지, 리뷰를 남겨 직관 경험을 다시 볼 수 있습니다.
- **통계와 랭킹**: 나의 직관 기록을 기반으로 승률, 승리 요정, 구장별 기록, 팬 비율 등을 확인할 수 있습니다.
- **실시간 응원톡**: 경기별 토크와 좋아요를 통해 다른 팬들과 실시간으로 반응을 나눌 수 있습니다.
- **승부 예측 및 보상**: 경기 시작 전에 승리 팀을 예측하고, 경기 종료 후 결과에 따라 포인트 보상을 받을 수 있도록 기획했습니다.

자세한 기능 설명은 [기능 설명 문서](backend/docs/features.md)에서 확인할 수 있습니다.

## 🛠️ 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.3
- **Build**: Gradle
- **Database**: MySQL, H2
- **Persistence**: Spring Data JPA, QueryDSL, Flyway
- **API Docs**: Springdoc OpenAPI
- **Monitoring**: Spring Boot Actuator, Micrometer, Prometheus, OpenTelemetry
- **Storage**: AWS S3
- **Crawler**: Playwright

## 🚀 실행 방법

```bash
cd backend
./gradlew :app:bootRun
```

크롤링 모듈은 다음 명령어로 실행할 수 있습니다.

```bash
cd backend
./gradlew :crawling:bootRun
```

## 👥 팀원 소개

|<img src="https://github.com/medAndro.png" width="125" />|<img src="https://github.com/ijh1298.png" width="125" />|<img src="https://github.com/jiyuneel.png" width="125" />|<img src="https://github.com/jjunh0.png" width="125" />|<img src="https://github.com/Starlight258.png" width="125" />|<img src="https://github.com/bowook.png" width="125" />|<img src="https://github.com/nourzoo.png" width="125" />|
|:---------:|:---------:|:---------:|:---------:|:---------:|:---------:|:---------:|
|[메다(장지형)](https://github.com/medAndro)|[크림(임준혁)](https://github.com/ijh1298)|[포르(이지윤)](https://github.com/jiyuneel)|[두리(김준호)](https://github.com/jjunh0)|[밍트(김명지)](https://github.com/Starlight258)|[우가(민보욱)](https://github.com/bowook)|[포라(이승연)](https://github.com/nourzoo)|
|Android|Android|Android|Backend|Backend|Backend|Backend|


## 💫 규칙들

### **🧑‍🤝‍🧑 TEAM CULTURE**

- **🐲 친절하게 말해주세용**

  → 논의가 과열되었을 때 용용체로 대화해용

- 🙉 **너의 목소리가 안 들려**

  → 무엇이든지 말해주세요

- **🍗 반마리보다 🐓한마리예요**

  → 회의는 존댓말로 진행해요

- **🤗 되면 대면해요**

  → 대면 협업을 우선해요


---

### **⏰ WORKFLOW**

- **📅 데일리 스크럼: 데일리 미팅 끝나고 바로**

  → 어제 한 일과 오늘 할 일을 공유해요

- 🍚 **수요일 == 외식 데이**

  → 다 같이 밥 먹어요


---

### **🗣️ COMMUNICATION**

- 🤔 **설득하거나 설득 당하거나**

  → 논의할 때 최소한의 입장을 가져요

- ✋ **세상에서 바보같은 질문은 없다**

  → 묻지 않고 넘기는 게 더 위험해요

- ⌛ **일과 시간은 팀과 시간을**

  → 일과 시간은 팀과 함께 움직여요

- ❤️ **좋아요가 좋아요**

  → 긍정적인 리액션이 좋아요


---

### **📚 KNOWLEDGE SHARING**

- ⬆️ **우리 함께 레벨업**

  → 알게 된 지식은 혼자 갖지 말고 공유해요

- 🌱 **기록하면 새록새록**

  → 모든 일을 기록해요
