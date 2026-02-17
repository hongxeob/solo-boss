# SoloBoss AI - 세션 핸드오프 문서

> 최종 업데이트: 2026-02-17
> 커밋: `0af2e85` (main, pushed to origin)

---

## 프로젝트 요약

1인 프리랜서/전문직 사업자용 AI CRM 백엔드.
카카오톡 스크린샷/음성 유입 → OCR/구조화 → 신뢰도 분기(자동 저장/검수) → 고객/상담/팔로업으로 확장.

## 현재 상태

### 완료된 작업

| # | 작업 | 상태 |
|---|------|------|
| 1 | 프로젝트 스캐폴딩 + CLAUDE.md | ✅ 완료 |
| 2 | 전체 도메인 모델 (엔티티, 리포지토리, 마이그레이션) | ✅ 완료 |
| 3 | Customer CRUD (서비스, 컨트롤러, DTO, 테스트) | ✅ 완료 |
| 4 | 스크린샷 OCR 추출 (Spring AI + Claude Vision) | ✅ 완료 (기본 경로) |
| 5 | 카카오 웹훅 수신 → OCR 처리 연동 | ✅ 완료 |
| 6 | Low-confidence 시 ReviewTask 자동 생성 | ✅ 완료 |
| 7 | Review 조회/해결 API (`/api/v1/reviews`) | ✅ 완료 |
| - | ktlint 14.0.1 + 포맷/검증 파이프라인 | ✅ 완료 |

### 다음 작업 (우선순위)

| # | 작업 | 상태 |
|---|------|------|
| A | 알림톡 API/서비스 (`POST /api/v1/notifications/alimtalk`) | 🔜 다음 |
| B | OCR 품질 이슈 템플릿(`OCR_*`) 분기 발송 | 🔜 다음 |
| C | Duplicate UX 정책(병합/Undo/안내) 백엔드 반영 | 예정 |
| D | Interaction CRUD (상담 기록) | 예정 |
| E | 상담 임베딩 + 벡터 저장소 실사용 | 예정 |
| F | 관계 메모리 검색 + 요약 | 예정 |
| G | 팔로업 메시지 생성/스케줄링 | 예정 |

---

## 이번 세션 핵심 변경점

### 1) Customer API 구현

- `POST /api/v1/customers`
- `GET /api/v1/customers/{customerId}`
- `GET /api/v1/customers`
- `PATCH /api/v1/customers/{customerId}`
- `DELETE /api/v1/customers/{customerId}`

주요 파일:
- `src/main/kotlin/com/soloboss/ai/application/customer/CustomerService.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/customer/CustomerController.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/customer/CustomerDtos.kt`

### 2) OCR 추출 유스케이스 구현

- `POST /api/v1/ocr/extract`
- `GET /api/v1/ocr/jobs/{jobId}`
- 멱등키(`channel_id:message_id`) 재요청 시 기존 작업 재사용
- 임계치: `overall_confidence >= 0.85` → `AUTO_SAVED`, 미만 → `NEEDS_REVIEW`

주요 파일:
- `src/main/kotlin/com/soloboss/ai/application/ocr/OcrExtractionService.kt`
- `src/main/kotlin/com/soloboss/ai/application/ocr/OcrModels.kt`
- `src/main/kotlin/com/soloboss/ai/infrastructure/ai/AnthropicOcrExtractor.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/ocr/OcrController.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/ocr/OcrDtos.kt`

### 3) 웹훅 유입 연동 구현

- `POST /api/v1/integrations/kakao/webhook`
- 시그니처 검증(현재 기본 구현체)
- 채널 기반 owner 매핑(현재 기본 구현체)
- webhook 수신 즉시 OCR 추출 파이프라인 호출

주요 파일:
- `src/main/kotlin/com/soloboss/ai/application/integration/KakaoWebhookService.kt`
- `src/main/kotlin/com/soloboss/ai/application/integration/KakaoWebhookModels.kt`
- `src/main/kotlin/com/soloboss/ai/infrastructure/external/DefaultKakaoIntegrations.kt`
- `src/main/kotlin/com/soloboss/ai/web/webhook/KakaoWebhookController.kt`

### 4) 검수(Review) 흐름 구현

- low-confidence 결과에서 `ReviewTask` 자동 생성
- 필드 confidence `< 0.7` 목록을 `uncertain_fields`에 저장
- 기본 만료시간: 생성 후 24시간
- `PATCH /api/v1/reviews/{reviewTaskId}/resolve` 시 `ingest_job`도 `NEEDS_REVIEW -> AUTO_SAVED`

주요 파일:
- `src/main/kotlin/com/soloboss/ai/application/review/ReviewService.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/review/ReviewController.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/review/ReviewDtos.kt`
- `src/main/kotlin/com/soloboss/ai/infrastructure/persistence/ReviewTaskRepository.kt`
- `src/main/kotlin/com/soloboss/ai/domain/interaction/IngestJobStatus.kt`

### 5) 공통 예외 처리 추가

- `EntityNotFoundException` → 404
- `MethodArgumentNotValidException` → 400

주요 파일:
- `src/main/kotlin/com/soloboss/ai/web/v1/common/ApiExceptionHandler.kt`

---

## 테스트 현황

신규 테스트:
- `src/test/kotlin/com/soloboss/ai/application/customer/CustomerServiceTest.kt`
- `src/test/kotlin/com/soloboss/ai/application/ocr/OcrExtractionServiceTest.kt`
- `src/test/kotlin/com/soloboss/ai/application/ocr/OcrExtractionServiceReviewTaskTest.kt`
- `src/test/kotlin/com/soloboss/ai/application/integration/KakaoWebhookServiceTest.kt`
- `src/test/kotlin/com/soloboss/ai/application/review/ReviewServiceTest.kt`

검증 명령:
```bash
./gradlew compileKotlin test ktlintCheck
```

결과: 성공

---

## UX/문서 반영 상태

### 이미 코드 반영됨
- `docs/ux-research/api/kakao-ingestion-events.md`의 핵심 흐름 중 webhook→ingest(OCR)→review 분기
- `overall_confidence 0.85` 자동 저장 임계치
- low-confidence 필드 기반 검수함 생성

### 아직 코드 반영 필요
- `docs/ux-research/notifications/alimtalk-templates.md`의 신규 템플릿
  - `OCR_TEXT_ONLY`
  - `OCR_IMAGE_BLURRY`
  - `OCR_IMAGE_EXPOSURE`
  - `OCR_NOT_CONVERSATION`
  - `OCR_MULTI_IMAGE_ORDER`
- 알림 발송 엔드포인트/서비스 부재
  - `POST /api/v1/notifications/alimtalk` 미구현
- Duplicate Input UX Guideline(병합/Undo/중복 안내) 미구현

---

## 상태 머신 주의사항

### IngestJob

현재 코드 기준:
```text
RECEIVED → OCR_DONE → STRUCTURED → AUTO_SAVED / NEEDS_REVIEW / FAILED
RECEIVED/OCR_DONE → FAILED
NEEDS_REVIEW → AUTO_SAVED (수동 검수 완료)
NEEDS_REVIEW → EXPIRED
```

### ReviewTask

```text
OPEN → IN_PROGRESS → RESOLVED
IN_PROGRESS → OPEN
OPEN/IN_PROGRESS → EXPIRED
```

### FollowUpTask

```text
SCHEDULED → DRAFT_READY → SENT / EDITING / SNOOZED
EDITING → SENT
SNOOZED → DRAFT_READY
SCHEDULED/DRAFT_READY/EDITING/SNOOZED → CANCELED
```

---

## 기술/운영 메모

- `domain/` 패키지는 순수 Kotlin 유지 (Spring 의존성 금지, JPA 어노테이션 제외)
- Flyway 기존 마이그레이션 수정 금지, 항상 신규 버전 추가
- ktlint 규칙 엄격 적용 (import order, trailing comma 등)
- 현재 웹훅 시그니처 검증은 placeholder 수준. 실 운영 전 HMAC 검증으로 교체 필요
- OCR extractor는 `ResourceLoader`로 `sourceUrl`을 로딩하므로 외부 URL/S3 접근 정책 확인 필요
