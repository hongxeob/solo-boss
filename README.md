# SoloBoss AI

SoloBoss AI는 1인 사업자용 AI CRM이지만, 이 레포의 핵심은 기능 소개보다  
**AI 오케스트레이션으로 제품을 설계/개발/명세화한 방식 자체**에 있습니다.

## 이 레포의 대표 메시지 🤖

단일 모델 호출이 아니라, 여러 AI/에이전트 흐름을 연결해 실제 서비스로 수렴한 경험을 담았습니다.

- `입력 오케스트레이션`: 카카오 webhook -> 멱등 처리 -> 비동기 작업 분배
- `이해 오케스트레이션`: OCR -> LLM 구조화 -> confidence scoring
- `의사결정 오케스트레이션`: 자동 저장 vs 검수함(HITL) 분기
- `행동 오케스트레이션`: 팔로업 초안/리마인드 + 주간 코칭 리포트 발송
- `문서 오케스트레이션`: UX 시나리오 -> API 이벤트 -> OpenAPI -> 백엔드 구현 프롬프트

채용/지원 맥락에서 이 프로젝트는  
**"AI를 붙인 서비스"가 아니라 "AI 오케스트레이션으로 제품을 운영 가능한 형태로 만든 경험"**을 보여주기 위한 포트폴리오입니다.

## 개발 방식 (Vibe Coding x Agent Orchestration) 🚀

이 프로젝트는 vibe coding을 빠른 탐색 도구로 쓰되, 결과를 에이전트 기반 계약 문서로 고정합니다.

1. 아이디어/요구사항을 짧은 루프로 탐색
2. UX 리서치/사용자 흐름을 문서화
3. 이벤트/API 계약을 분리 문서로 고정
4. OpenAPI와 구현 프롬프트로 백엔드 실행 단위 생성
5. 테스트/빌드 게이트로 안정화

## 에이전트/툴 역할 분담 (실전 운영) 🧩

이 프로젝트는 단일 도구에 의존하지 않고, 각 AI 도구를 역할에 맞게 조합해 개발했습니다.

- `Claude Code`: 요구사항 해석, UX/사용자 흐름 설계, 문서화 초안 작성
- `Codex`: 코드/문서 수정 실행, 레포 반영, 검증 및 커밋 작업
- `Gemini CLI`: 대안 탐색, 비교 관점 점검, 보조 아이디어 확장

핵심은 "어떤 모델이 더 좋다"가 아니라,  
**역할 기반 분업으로 탐색 속도와 구현 안정성을 함께 확보한 오케스트레이션 경험**입니다.

## AI 운영 문서 기준 (AGENTS/SKILL) 📘

이 레포는 AI가 일관되게 일하도록 문서 기반 운영 규칙을 명시합니다.

- `AGENTS.md`: 작업 범위, 우선순위, 응답/수정 규칙
- `SKILL.md` 계열: 상황별 워크플로우(브레인스토밍, 검증, 코드리뷰, 계획 수립)
- 기획/명세 문서:
  - `docs/ux-research/...`
  - `docs/ai-spec/...`

즉, 코드만 남기는 프로젝트가 아니라  
**AI가 재현 가능하게 협업하도록 프로세스까지 함께 설계한 프로젝트**를 지향합니다.

## 제품 표면 (Product Surface) ✨

- 카카오톡 상담 입력(스크린샷/음성)
- AI 자동 등록 + 검수 분기
- 팔로업 메시지/주간 코칭 리포트

## 프로젝트 구조 📁

- `src/main` : Spring Boot/Kotlin 백엔드
- `src/test` : 백엔드 테스트
- `frontend` : Next.js 웹 콘솔
- `docs/ux-research` : UX/이벤트/API/리포트 문서
- `docs/ai-spec` : AI 처리 스펙

## 기술 스택 🧱

- Backend: Kotlin, Spring Boot 3.4, JPA, PostgreSQL(pgvector), Flyway
- AI: Spring AI, LLM 기반 추출/요약/생성
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

- 완료: 카카오톡 중심 UX/상태머신/오류 페르소나 설계
- 완료: 주간 코칭 리포트 기획 + API 이벤트 명세 + OpenAPI 초안
- 진행 중: 백엔드 통합 구현 및 운영 검증
