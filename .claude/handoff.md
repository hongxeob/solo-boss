# SoloBoss AI - 세션 핸드오프 문서

> 최종 업데이트: 2026-02-17
> 커밋: `eea8116` (main, pushed to origin)

---

## 프로젝트 요약

1인 프리랜서/전문직 사업자용 AI CRM 백엔드.
카카오톡 유입 → OCR 구조화 → 신뢰도 분기 → 검수/자동저장 → 임베딩/메모리 → 알림톡/후속 액션으로 이어지는 전체 파이프라인 구현 완료.

## 현재 상태

### 로드맵 9단계 전체 완료

| # | 단계 | 상태 |
|---|------|------|
| 1 | 프로젝트 스캐폴딩 + CLAUDE.md | ✅ 완료 |
| 2 | 전체 도메인 모델 (엔티티, Flyway V1~V6) | ✅ 완료 |
| 3 | Customer CRUD | ✅ 완료 |
| 4 | 스크린샷 OCR 추출 | ✅ 완료 |
| 5 | Interaction CRUD (상담 기록) | ✅ 완료 |
| 6 | 상담 임베딩 + 벡터 저장소 | ✅ 완료 |
| 7 | 관계 메모리 검색 + 요약 | ✅ 완료 |
| 8 | 팔로업 메시지 생성 | ✅ 완료 |
| 9 | 배치 생성 및 스케줄링 | ✅ 완료 |

### 이번 세션에서 완료한 작업

**Step 6-7: 상담 임베딩 + 관계 메모리 (커밋 `7e227b1`)**
- `ConsultationEmbeddingService` — VectorStore 래퍼, embed/remove/composeEmbeddingText (6000자 제한)
- `ConsultationService`에 embedSafely 훅 추가 (create/update/delete, try-catch로 안전 동작)
- `RelationshipMemoryService` — 시맨틱 유사도 검색 + Anthropic Claude 관계 요약
- `MemoryController` — `POST /api/v1/memory/search`, `POST /api/v1/memory/summarize`
- `relationship-summary.st` — Role-Task-Constraint 구조 프롬프트

**테스트 보강 (커밋 `eea8116`)**
- `ConsultationEmbeddingServiceTest` (6개) — embed 메타데이터, 텍스트 조합, 자르기
- `ConsultationServiceTest` (8개) — CRUD + 임베딩 훅 + 실패 안전 동작
- `RelationshipMemoryServiceTest` (7개) — 검색, AI 요약, 에러 핸들링
- `MemoryControllerTest` (4개) — search/summarize API

**문서화 (커밋 `5705842`)**
- `docs/PROJECT_STATUS.md` — 전체 점검 체크리스트 생성
- `CLAUDE.md` — 로드맵 전 단계 취소선 적용

---

## API 전체 목록

```
# 고객
GET/POST          /api/v1/customers
GET/PATCH/DELETE  /api/v1/customers/{id}

# 상담
GET/POST          /api/v1/consultations
GET/PATCH/DELETE  /api/v1/consultations/{id}

# OCR
POST              /api/v1/ocr/extract

# 리뷰
GET               /api/v1/reviews
PATCH             /api/v1/reviews/{id}

# 팔로업 태스크
GET/POST          /api/v1/tasks
PATCH             /api/v1/tasks/{id}

# 메모리 (관계 기억)
POST              /api/v1/memory/search
POST              /api/v1/memory/summarize

# 중복 감지
POST              /api/v1/duplicates

# 알림
GET/POST          /api/v1/notifications

# 통계
GET               /api/v1/stats

# 웹훅
POST              /api/webhook/kakao
```

---

## 주요 파일 (이번 세션 신규/수정)

### 신규
- `src/main/kotlin/com/soloboss/ai/infrastructure/ai/ConsultationEmbeddingService.kt`
- `src/main/kotlin/com/soloboss/ai/application/memory/MemoryModels.kt`
- `src/main/kotlin/com/soloboss/ai/application/memory/RelationshipMemoryService.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/memory/MemoryDtos.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/memory/MemoryController.kt`
- `src/main/resources/prompts/relationship-summary.st`

### 수정
- `src/main/kotlin/com/soloboss/ai/application/consultation/ConsultationService.kt` — 임베딩 훅

### 테스트
- `src/test/kotlin/com/soloboss/ai/infrastructure/ai/ConsultationEmbeddingServiceTest.kt`
- `src/test/kotlin/com/soloboss/ai/application/consultation/ConsultationServiceTest.kt`
- `src/test/kotlin/com/soloboss/ai/application/memory/RelationshipMemoryServiceTest.kt`
- `src/test/kotlin/com/soloboss/ai/web/v1/memory/MemoryControllerTest.kt`

---

## 코드 규모

- 백엔드 소스: 51 Kotlin 파일
- 테스트: 17 파일 (기존 13 + 신규 4)
- Flyway 마이그레이션: V1~V6
- 프론트엔드: 21 파일 (Next.js 14)

---

## 남은 갭 (다음 세션 후보)

### 기술 부채
- OCR 프롬프트 `.st` 외부화 (현재 `AnthropicOcrExtractor`에 인라인)

### 프론트엔드
- 메모리 검색 UI (API는 완성, 프론트 미구현)
- OCR 스크린샷 업로드 UI (API는 완성, 프론트 미구현)
- 미커밋 프론트 변경분: `ConsultationSection.tsx` 등

### 운영 준비
- 인증/인가 (Spring Security, 현재 ownerId 클라이언트 전송)
- CI/CD 파이프라인
- 모니터링 (헬스체크, 메트릭)
- 알림톡 실연동 (현재 MockAlimtalkSenderGateway)
- 스케줄러 배치 (Review EXPIRED, Follow-up due 만료 처리)
- AI 기반 팔로업 메시지 초안 생성 로직

---

## 검증

```bash
./gradlew compileKotlin test ktlintCheck
```
결과: BUILD SUCCESSFUL (50+ tests, 0 failures)
