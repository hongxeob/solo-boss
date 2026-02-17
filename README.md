# SoloBoss AI 🚀

SoloBoss AI는 1인 사업자를 위한 올인원 AI CRM입니다.  
고객 상담 데이터를 카카오톡에서 바로 받아 자동 정리하고, 팔로업 실행까지 이어지게 설계했습니다.

## Why SoloBoss AI 💡

1인 사업자는 보통 영업, 상담, 운영, 정산을 혼자 처리합니다.  
SoloBoss AI는 "기록하는 시간"을 줄이고 "후속 행동"에 집중하게 만듭니다.

- ✅ 입력은 카카오톡에서 끝난다.
- ✅ AI가 고객 기록을 구조화해 적재한다.
- ✅ 필요한 순간에 팔로업 초안을 제안한다.

## Core Features ✨

1. 상담 스크린샷/음성 자동 등록
- 카카오톡 채널로 받은 스크린샷/음성을 OCR + LLM으로 구조화
- 고객/상담/다음 행동을 CRM 데이터로 자동 저장

2. 팔로업 메시지 초안 생성
- 특정 시점(D-1, 당일 등)에 맞춰 초안 생성
- 카카오 알림톡에서 바로 전송/수정/미루기

3. 관계 메모리 요약
- 과거 상담 이력을 요약해 고객 맥락 유지
- 다음 상담 전에 핵심 포인트를 빠르게 확인

## UX Principle (MVP) 🎯

- Primary Channel: 카카오톡 💬
- Secondary Console: 모바일 최적화 최소 웹 콘솔 📱
- Notification: 카카오 알림톡 🔔
- Confidence Hybrid:
  - `overall_confidence >= 0.85` -> 자동 저장
  - `overall_confidence < 0.85` -> 검수 요청

## End-to-End Flow 🔄

1. 사장님이 카카오톡에 상담 스크린샷/음성을 전송 📤
2. Webhook 수신 후 즉시 접수 알림 발송 ⚡
3. 비동기 파이프라인으로 OCR -> LLM 구조화 -> 신뢰도 평가 🤖
4. 자동 저장 또는 검수 분기 처리 🧭
5. 처리 결과를 알림톡으로 안내 📩
6. 관계 메모리/임베딩/팔로업 태스크 갱신 🧠

## Tech Stack 🧱

- Kotlin 2.1
- Spring Boot 3.4.x
- Spring AI 1.0.x
- PostgreSQL 16 + pgvector
- Flyway
- Gradle (Kotlin DSL)

## Quick Start 🛠️

### Prerequisites

- Java 21
- Docker / Docker Compose

### 1) Start PostgreSQL (pgvector) 🐘

```bash
docker compose up -d
```

### 2) Run Application ▶️

```bash
./gradlew bootRun
```

### 3) Run Tests ✅

```bash
./gradlew test
```

## Project Documents 📚

- UX research pack:
  - `/Users/mediquitous/Desktop/project/solo-boss/ux-research/README.md`
  - `/Users/mediquitous/Desktop/project/solo-boss/ux-research/notifications/alimtalk-templates.md`
  - `/Users/mediquitous/Desktop/project/solo-boss/ux-research/api/kakao-ingestion-events.md`
  - `/Users/mediquitous/Desktop/project/solo-boss/ux-research/diagrams/state-transitions.md`

- Additional specs:
  - `/Users/mediquitous/Desktop/project/solo-boss/docs/ai-spec/OCR_EXTRACTION_GUIDE.md`
  - `/Users/mediquitous/Desktop/project/solo-boss/docs/ai-spec/FREELANCER_NOTE_EXTRACTION.md`

## Status 🧪

MVP 설계 단계에서 카카오톡 중심 사용자 흐름과 이벤트 명세를 우선 확정한 상태입니다.
