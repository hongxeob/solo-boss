# SoloBoss AI

1인 프리랜서/전문직 사업자를 위한 올인원 AI CRM 백엔드.
카카오톡 상담 스크린샷에서 고객 정보를 자동 추출하고, 관계 기억을 관리하며, 팔로업 메시지를 생성한다.

## 기술 스택

- **Language**: Kotlin 2.1, Java 21
- **Framework**: Spring Boot 3.4
- **AI**: Spring AI 1.0 (Anthropic Claude = 채팅/비전, OpenAI = 임베딩)
- **Database**: PostgreSQL 16 + pgvector
- **Build**: Gradle Kotlin DSL, Flyway (DB 마이그레이션)
- **Infra**: Docker Compose (로컬 개발)

## 핵심 기능

1. **스크린샷 OCR → 고객 등록**: 카카오톡 채팅 스크린샷 업로드 → Claude Vision으로 고객 정보 추출 → 사용자 확인 후 등록 (2단계 프로세스)
2. **팔로업 메시지 생성**: 고객 상담 이력 + 벡터 검색 컨텍스트를 기반으로 AI가 맞춤 팔로업 초안 작성
3. **관계 메모리**: 상담 내용을 임베딩하여 pgvector에 저장, 시맨틱 검색으로 과거 이력 요약 제공

## 패키지 구조

```
com.soloboss.ai/
├── domain/                 # 비즈니스 핵심 로직 (순수 코틀린)
│   ├── customer/           # 고객 엔티티, 서비스
│   ├── interaction/        # 상담 기록, 신뢰도 로직, 상태
│   └── task/               # 팔로업 할 일, 알림 스케줄링
├── infrastructure/         # 외부 연동 (Spring AI, DB, 외부 API)
│   ├── ai/                 # ChatClient 설정, 프롬프트 템플릿
│   ├── persistence/        # Repository 구현체
│   └── external/           # 카카오 Webhook 핸들러, S3 업로더
├── application/            # 유스케이스 (도메인과 인프라 연결)
│   └── ocr/                # OCR 실행 및 결과 파싱 오케스트레이션
└── web/                    # API 컨트롤러 및 DTO
    ├── v1/                 # 모바일 웹 콘솔용 API
    └── webhook/            # 카카오톡 전용 엔드포인트
```

**원칙**: domain은 순수 코틀린으로 Spring 의존성 없이 유지. infrastructure가 외부 세계와의 연동을 담당. application이 유스케이스 오케스트레이션.

## 프로젝트 루트 디렉토리

- `docs/` — UX 리서치, 프론트엔드 설계 등 외부 AI(Gemini, GPT)로 작성한 문서 모음

## API 설계 원칙

- REST API, `/api/v1/` prefix
- 스크린샷 OCR은 2단계: `POST /extract` (추출) → `POST /customers` (확인 후 등록)
- JSON 응답, 페이지네이션은 Spring Data의 Pageable 사용

## AI 통합 패턴

- **채팅/비전**: Anthropic Claude (spring-ai-starter-model-anthropic)
- **임베딩**: OpenAI text-embedding-3-small (spring-ai-starter-model-openai, chat disabled)
- **벡터 저장소**: PgVectorStore (spring-ai-starter-vector-store-pgvector), metadata 필터링으로 고객별 검색
- 프롬프트는 `src/main/resources/prompts/` 에 StringTemplate(.st) 파일로 외부화

## 개발 환경 설정

```bash
# PostgreSQL + pgvector 시작
docker compose up -d

# 환경변수 설정 (또는 .env 파일)
export ANTHROPIC_API_KEY=your-key
export OPENAI_API_KEY=your-key

# 빌드 및 실행
./gradlew bootRun
```

## 구현 로드맵

1. ~~프로젝트 스캐폴딩 + CLAUDE.md~~ ← 현재 단계
2. Customer CRUD (엔티티, 리포지토리, 서비스, 컨트롤러, Flyway 마이그레이션)
3. 스크린샷 OCR 추출 (Spring AI multimodal + Claude Vision)
4. Interaction CRUD (상담 기록)
5. 상담 임베딩 + 벡터 저장소 설정
6. 관계 메모리 검색 + 요약
7. 팔로업 메시지 생성
8. 배치 생성 및 스케줄링

## 코드 컨벤션

- Kotlin 코딩 스타일: Kotlin 공식 코딩 컨벤션
- 엔티티 ID: UUID (gen_random_uuid)
- 시간: OffsetDateTime (TIMESTAMPTZ)
- DTO: data class, request/response 분리
- 테스트: JUnit 5 + spring-boot-starter-test
