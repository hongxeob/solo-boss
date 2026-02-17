# SoloBoss AI - 세션 핸드오프 문서

> 최종 업데이트: 2026-02-17
> 커밋: `6cea4ac` (main, pushed to origin)

---

## 프로젝트 요약

1인 프리랜서/전문직 사업자용 AI CRM 백엔드.
카카오톡 스크린샷 → OCR → 고객 자동 등록 → 관계 메모리 → 팔로업 메시지 생성.

## 현재 상태

### 완료된 작업 (로드맵 1~2단계)

| # | 작업 | 상태 |
|---|------|------|
| 1 | 프로젝트 스캐폴딩 + CLAUDE.md | ✅ 완료 |
| 2 | 전체 도메인 모델 (엔티티, 리포지토리, 마이그레이션) | ✅ 완료 |
| - | ktlint 14.0.1 플러그인 + 코드 포맷팅 | ✅ 완료 |
| - | docs/ai-spec 문서 (OCR 가이드, 노트 추출 스펙) | ✅ 완료 |
| - | 프론트엔드 초기 구조 (Next.js) | ✅ 완료 |

### 다음 작업 (로드맵 3단계~)

| # | 작업 | 상태 |
|---|------|------|
| 3 | Customer CRUD (서비스, 컨트롤러, DTO) | 🔜 다음 |
| 4 | 스크린샷 OCR 추출 (Spring AI + Claude Vision) | 예정 |
| 5 | Interaction CRUD (상담 기록) | 예정 |
| 6 | 상담 임베딩 + 벡터 저장소 설정 | 예정 |
| 7 | 관계 메모리 검색 + 요약 | 예정 |
| 8 | 팔로업 메시지 생성 | 예정 |
| 9 | 배치 생성 및 스케줄링 | 예정 |

---

## 기술 스택

- **Kotlin 2.1** + **Spring Boot 3.4.1** + **Java 21**
- **Spring AI 1.0.0** (Anthropic=chat/vision, OpenAI=embeddings)
- **PostgreSQL 16 + pgvector**, Flyway, Gradle 8.12
- **ktlint 14.0.1** (Gradle 플러그인)
- **프론트엔드**: Next.js 14.2.3 + TypeScript + Tailwind CSS

### Spring AI 아티팩트명 (1.0.0 GA 기준)

```
spring-ai-starter-model-anthropic       (NOT spring-ai-anthropic-spring-boot-starter)
spring-ai-starter-model-openai          (NOT spring-ai-openai-spring-boot-starter)
spring-ai-starter-vector-store-pgvector (NOT spring-ai-pgvector-store-spring-boot-starter)
```

---

## 엔티티 관계도

```
Customer (1) <--- (*) Consultation
    |                     |
    |                     +--- (0..1) IngestJob
    |
    +--- (*) FollowUpTask

IngestJob (1) --- (0..1) ReviewTask
```

### 핵심 설계 결정

1. **엔티티 참조**: UUID ID 참조 (`@ManyToOne` 대신), FK는 SQL에서 강제
2. **JPA 엔티티**: `class` 사용 (`data class` X — equals/hashCode/copy 문제 방지)
3. **멀티테넌시**: 모든 엔티티에 `owner_id` 컬럼
4. **신뢰도**: `overall_confidence` DOUBLE 컬럼 (0.85 임계치), 필드별은 JSONB
5. **벡터 임베딩**: Spring AI PgVectorStore 기본 테이블 (metadata 필터링)
6. **멱등 처리**: IngestJob에 `idempotency_key` UNIQUE 제약

---

## 상태 머신 (3개)

### IngestJob

```
RECEIVED → OCR_DONE → STRUCTURED → AUTO_SAVED  (confidence >= 0.85)
                                 → NEEDS_REVIEW (confidence < 0.85)
                                 → FAILED
RECEIVED/OCR_DONE → FAILED
NEEDS_REVIEW → EXPIRED
```

### ReviewTask

```
OPEN → IN_PROGRESS → RESOLVED
IN_PROGRESS → OPEN (임시 이탈)
OPEN/IN_PROGRESS → EXPIRED
```

### FollowUpTask

```
SCHEDULED → DRAFT_READY → SENT / EDITING / SNOOZED
EDITING → SENT
SNOOZED → DRAFT_READY
SCHEDULED/DRAFT_READY/EDITING/SNOOZED → CANCELED
```

---

## 파일 구조 (백엔드 Kotlin)

```
src/main/kotlin/com/soloboss/ai/
├── SoloBossApplication.kt
├── domain/
│   ├── customer/
│   │   ├── Customer.kt              # JPA 엔티티
│   │   └── CustomerSource.kt        # enum: KAKAO, MANUAL, IMPORT
│   ├── interaction/
│   │   ├── Consultation.kt          # JPA 엔티티 (상담 기록)
│   │   ├── ExtractionResult.kt      # VO: ConfidenceField<T>, SummaryField
│   │   ├── IngestJob.kt             # JPA 엔티티 (OCR 파이프라인)
│   │   ├── IngestJobStatus.kt       # enum + 상태 머신 (7개 상태)
│   │   ├── ReviewTask.kt            # JPA 엔티티 (사용자 검토)
│   │   ├── ReviewTaskStatus.kt      # enum + 상태 머신 (4개 상태)
│   │   └── SourceType.kt            # enum: IMAGE, VOICE
│   └── task/
│       ├── FollowUpTask.kt          # JPA 엔티티 (팔로업)
│       └── FollowUpTaskStatus.kt    # enum + 상태 머신 (6개 상태)
├── infrastructure/
│   ├── ai/                           # (미구현) ChatClient 설정
│   ├── external/                     # (미구현) 카카오 Webhook, S3
│   └── persistence/
│       ├── CustomerRepository.kt
│       ├── IngestJobRepository.kt
│       ├── ConsultationRepository.kt
│       ├── ReviewTaskRepository.kt
│       └── FollowUpTaskRepository.kt
├── application/
│   └── ocr/                          # (미구현) OCR 유스케이스
└── web/
    ├── v1/                           # (미구현) REST 컨트롤러
    └── webhook/                      # (미구현) 카카오 웹훅
```

## 파일 구조 (프론트엔드)

```
frontend/
├── app/page.tsx              # 3탭 메인 페이지 (오늘/검수함/고객)
├── components/
│   ├── BottomNav.tsx         # 하단 탭 네비게이션
│   ├── ClientList.tsx        # 고객 목록
│   ├── ReviewBox.tsx         # 신뢰도 낮은 항목 검수
│   └── TodayTasks.tsx        # 오늘의 팔로업
├── types/index.ts            # TabType, MessageDraft, ReviewItem
├── package.json              # Next.js 14.2.3, Tailwind, Lucide
└── tsconfig.json
```

## DB 마이그레이션

| 파일 | 테이블 | 핵심 |
|------|--------|------|
| V1__init_extensions.sql | - | pgcrypto + vector 확장 |
| V2__create_customers.sql | customers | owner_id idx, kakao_user_key partial unique |
| V3__create_ingest_jobs.sql | ingest_jobs | idempotency_key UNIQUE, extraction_result JSONB |
| V4__create_consultations.sql | consultations | FK → customers, ingest_jobs |
| V5__create_review_tasks.sql | review_tasks | FK → ingest_jobs (unique 1:1) |
| V6__create_follow_up_tasks.sql | follow_up_tasks | FK → customers, consultations |

---

## 설정 파일 요약

### application.yml 핵심

- DB: `jdbc:postgresql://localhost:5432/soloboss` (user/pw: soloboss)
- JPA: `ddl-auto: validate` (Flyway가 스키마 관리)
- Anthropic: `claude-sonnet-4-20250514`, max-tokens 4096
- OpenAI: `text-embedding-3-small` (chat disabled)
- PgVector: HNSW, COSINE_DISTANCE, 1536 dimensions
- 파일 업로드: max 10MB/file, 30MB/request

### docker-compose.yml

- `pgvector/pgvector:pg16` 이미지
- Port 5432, DB/User/PW: soloboss

---

## 개발 환경 시작

```bash
cd /Users/mediquitous/Desktop/project/solo-boss

# DB 시작
docker compose up -d

# 백엔드 실행
export ANTHROPIC_API_KEY=<key>
export OPENAI_API_KEY=<key>
./gradlew bootRun

# 프론트엔드 실행 (별도 터미널)
cd frontend && npm install && npm run dev
```

## 빌드 검증

```bash
./gradlew compileKotlin    # 컴파일 확인
./gradlew ktlintCheck      # 린트 확인
./gradlew ktlintFormat     # 자동 포맷팅
```

---

## docs/ai-spec 문서 요약

### OCR_EXTRACTION_GUIDE.md
- Spring AI 1.0.0 + Gemini/Claude Vision 기반 이미지→구조화 추출 패턴
- BeanOutputConverter로 JSON 자동 파싱
- Role-Task-Constraint-Output 프롬프트 구조

### FREELANCER_NOTE_EXTRACTION.md
- 필드별 신뢰도(ConfidenceField) 스키마 정의
- 신뢰도 0.7 미만 → 노란색 경고, 사용자 수정 시 1.0 설정
- ConsultationExtraction 레코드 구조 (Java 예시 → Kotlin ExtractionResult로 구현됨)

---

## 주의사항

- `domain/` 패키지는 순수 Kotlin — Spring 의존성 금지 (JPA 어노테이션 제외)
- 상태 전이는 반드시 enum의 `transitionTo()` 메서드를 통해 수행
- `data class`는 VO에만 사용, JPA 엔티티는 일반 `class`
- Flyway 마이그레이션 파일은 절대 수정하지 말 것 (새 버전으로 추가)
- ktlint: wildcard import(`*`) 금지, trailing comma 필수
