# SoloBoss AI

SoloBoss AI는 카카오톡 상담 입력을 중심으로 동작하는 1인 사업자용 AI CRM입니다.  
핵심은 모델 1회 호출이 아니라, **OCR/LLM/스코어링/알림/리포트**를 연결한 AI 오케스트레이션으로 실제 영업 실행까지 이어지게 만드는 것입니다.

## 프로젝트 소개

이 프로젝트는 "고객 관리 자동화"보다 한 단계 더 나아가,  
"이번 주에 누구에게 먼저 연락해야 매출이 올라가는가"를 제시하는 **실행형 AI 제품**을 목표로 합니다.

- 입력 채널: 카카오톡(상담 스크린샷/음성)
- 처리 채널: OCR + 구조화 추출 + 신뢰도 기반 자동/검수 분기
- 실행 채널: 팔로업 메시지 초안 + 주간 코칭 리포트(알림톡)

## 이 프로젝트가 보여주는 점 (AI Orchestration Experience) 🤖

- Ingestion 오케스트레이션: webhook 수신, 멱등 처리, 비동기 파이프라인
- Understanding 오케스트레이션: `OCR -> LLM 구조화 -> confidence scoring`
- Decision 오케스트레이션: 자동 저장 vs 검수함 분기(HITL)
- Action 오케스트레이션: 팔로업 리마인드/초안 + 월요일 코칭 리포트
- Spec 오케스트레이션: UX 리서치 문서 -> 이벤트 명세 -> OpenAPI -> 백엔드 구현 프롬프트 연결

채용/지원 관점에서 보면, 이 레포는 "AI 기능 추가"가 아니라  
**AI를 운영 가능한 제품 흐름으로 설계하고 문서화한 경험**을 보여주는 포트폴리오 성격을 가집니다.

## 개발 방식 (Vibe Coding + AI) 🚀

- Vibe coding으로 빠르게 가설을 만들고 사용자 시나리오를 먼저 확정
- 멀티 에이전트/AI 오케스트레이션으로 기획-명세-구현 요청을 연결
- 결과물은 문서 계약(API/Event/OpenAPI)과 품질 게이트로 수렴
- "빠른 실험 + 안정적 운영"을 동시에 만족하는 방식으로 개발

## 핵심 기능

1. 상담 스크린샷/음성 자동 등록  
2. 시점 기반 팔로업 메시지 초안 생성  
3. 관계 메모리 요약 + 주간 영업 코칭 리포트

## 프로젝트 구조 📁

- `src/main` : Spring Boot/Kotlin 백엔드
- `src/test` : 백엔드 테스트
- `frontend` : Next.js 웹 콘솔
- `docs/ux-research` : UX/이벤트/API/리포트 기획 문서
- `docs/ai-spec` : AI 처리 스펙 문서

## 기술 스택 🧱

- Backend: Kotlin, Spring Boot 3.4, JPA, PostgreSQL(pgvector), Flyway
- AI: Spring AI, LLM 기반 구조화/요약/초안 생성
- Frontend: Next.js 14, React, TypeScript
- Tooling: Gradle, Docker Compose

## 로컬 실행 🛠️

### 1) 인프라 실행

```bash
docker compose up -d
```

### 2) 백엔드 실행

```bash
./gradlew bootRun
```

### 3) 프론트 실행

```bash
cd frontend
npm install
npm run dev
```

## 품질 게이트 ✅

```bash
./gradlew test
cd frontend && npm run lint && npm run build
```

## 문서 맵 📚

- `docs/ux-research/README.md`
- `docs/ux-research/api/kakao-ingestion-events.md`
- `docs/ux-research/api/weekly-coaching-report-events.md`
- `docs/ux-research/api/openapi-weekly-coaching-report.yaml`
- `docs/ux-research/api/backend-ai-prompt-weekly-report.md`

## 현재 진행 상태 (2026-02-21 기준) 🧪

- 완료: 카카오톡 중심 UX 플로우/상태머신/오류 페르소나 설계
- 완료: 주간 코칭 리포트 기획 + API 이벤트 명세 + OpenAPI 초안
- 진행 중: 백엔드 통합 구현 및 운영 기준 검증
