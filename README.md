# SoloBoss AI 🤖

**AI 오케스트레이션으로 개발하는 1인 사업자용 올인원 CRM.**  
카카오톡 상담 데이터가 들어오면, AI 파이프라인이 자동으로 해석하고 저장하고 다음 행동(팔로업)까지 제안합니다.

## 프로젝트 소개

SoloBoss AI는 "기록"보다 "실행"이 중요한 1인 사업자를 위해 만들어졌습니다.

- 상담 캡처/음성 입력은 카카오톡에서 끝
- AI가 고객 기록과 우선순위를 자동 정리
- 매주 코칭 리포트로 매출/행동을 함께 제시

## AI-First Product Vision 🚀

이 프로젝트는 기능 구현 자체뿐 아니라 **AI 오케스트레이션 기반 제품 개발 방식**을 목표로 합니다.

- Multi-step AI pipeline: `OCR -> 구조화 추출 -> 신뢰도 스코어링 -> 자동 저장/검수 분기`
- Action AI: `팔로업 메시지 생성`, `주간 영업 코칭 리포트`
- Human-in-the-loop: 신뢰도 낮은 건만 짧은 검수
- Vibe coding + spec-driven 문서화: 아이디어 -> UX 리서치 -> API 스펙 -> OpenAPI -> handoff까지 연동

## Core Features ✨

1. 상담 스크린샷/음성 자동 등록  
카카오톡으로 받은 자료를 AI가 CRM 스키마로 구조화하고 자동 저장합니다.

2. 팔로업 메시지 초안 생성  
고객 상태/시점에 맞춰 알림톡 기반 후속 메시지를 제안합니다.

3. 관계 메모리 + 주간 코칭 리포트  
상담 이력을 요약하고, 매주 월요일 "성과 + 바로 할 일"을 코치형으로 제공합니다.

## UX Strategy (MVP) 🎯

- Primary: 카카오톡 💬
- Secondary: 모바일 최적화 최소 웹 콘솔 📱
- Notification: 카카오 알림톡 🔔
- Confidence Hybrid:
  - `overall_confidence >= 0.85` -> 자동 저장
  - `overall_confidence < 0.85` -> 검수 요청

## AI Orchestration Snapshot 🔄

1. User input (Kakao): 스크린샷/음성 전송
2. Ingestion: webhook 수신 + 멱등 처리
3. Understanding: OCR + LLM 구조화 + confidence 평가
4. Decision: 자동 저장 vs 검수 요청
5. Action: 팔로업 초안/리마인드/주간 리포트 발송
6. Learning loop: 결과가 다음 우선순위/확률 계산에 반영

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

## Documentation Map 📚

- UX/Flow specs: `docs/ux-research/README.md`
- Kakao ingestion API/events: `docs/ux-research/api/kakao-ingestion-events.md`
- Weekly coaching report API/events: `docs/ux-research/api/weekly-coaching-report-events.md`
- Weekly report OpenAPI draft: `docs/ux-research/api/openapi-weekly-coaching-report.yaml`
- Backend AI implementation prompt: `docs/ux-research/api/backend-ai-prompt-weekly-report.md`

## Current Status 🧪

- 카카오톡 중심 UX와 이벤트 상태 머신 설계 완료
- 알림톡 카피/오류 페르소나/중복 UX 가이드 정리 완료
- 주간 코칭 리포트 기획 및 OpenAPI 초안 완료
- 백엔드 통합 구현 진행 중
