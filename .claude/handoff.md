# SoloBoss AI - 세션 핸드오프 문서

> 최종 업데이트: 2026-02-17
> 커밋: `59799bb` (main, pushed to origin)

---

## 프로젝트 요약

1인 프리랜서/전문직 사업자용 AI CRM 백엔드.
카카오톡 유입 → OCR 구조화 → 신뢰도 분기 → 검수/자동저장 → 알림톡/후속 액션으로 이어지는 흐름을 구현 중.

## 현재 상태

### 이번까지 완료된 핵심

1. Customer CRUD API 완료
- `POST /api/v1/customers`
- `GET /api/v1/customers`
- `GET /api/v1/customers/{customerId}`
- `PATCH /api/v1/customers/{customerId}`
- `DELETE /api/v1/customers/{customerId}`

2. OCR/수집 파이프라인 완료
- `POST /api/v1/ocr/extract`
- `GET /api/v1/ocr/jobs/{jobId}`
- 임계치: `overall_confidence >= 0.85` 자동저장
- low confidence 시 `ReviewTask` 자동 생성

3. 카카오 웹훅 연동 완료
- `POST /api/v1/integrations/kakao/webhook`
- HMAC 시그니처 검증(`kakao.webhook.secret` 설정 시)
- 멱등키 기반 처리

4. 알림톡 Mock 서비스 완료
- `POST /api/v1/notifications/alimtalk`
- 템플릿 필수 변수 검증
- webhook/OCR 흐름에서 자동 발송 연결

5. 품질 이슈 알림 분기 완료
- `OCR_TEXT_ONLY`
- `OCR_IMAGE_BLURRY`
- `OCR_IMAGE_EXPOSURE`
- `OCR_NOT_CONVERSATION`
- `OCR_MULTI_IMAGE_ORDER`

6. Review 해결 시 실제 반영 강화
- `PATCH /api/v1/reviews/{reviewTaskId}/resolve`
- resolve 시 Customer upsert + Consultation 생성/갱신 + Ingest 상태 반영

7. Tasks/Stats API 추가
- `GET /api/v1/tasks?ownerId=...`
- `POST /api/v1/tasks/{taskId}/send?ownerId=...`
- `POST /api/v1/tasks/{taskId}/snooze?ownerId=...`
- `GET /api/v1/stats?ownerId=...`

8. Duplicate 정책 1차 구현
- 완전 중복 안내
- 준중복 병합 판정(시간창 기반)
- Undo API: `POST /api/v1/duplicates/{undoToken}/undo?ownerId=...`
- 중복 타임라인 이벤트(메모리 기반)

---

## CLAUDE.md 로드맵 점검

### 1. 프로젝트 스캐폴딩 + CLAUDE.md
- 상태: 완료

### 2. 전체 도메인 모델 (엔티티, 리포지토리, Flyway)
- 상태: 완료

### 3. Customer CRUD
- 상태: 완료

### 4. 스크린샷 OCR 추출
- 상태: 완료
- 비고: Anthropic 기반 extractor + 구조화 파싱 동작

### 5. Interaction CRUD (상담 기록)
- 상태: 부분 완료
- 완료: Review resolve 시 Consultation 생성/갱신
- 미완: Consultation 전용 CRUD API는 없음

### 6. 상담 임베딩 + 벡터 저장소 설정
- 상태: 부분 완료
- 완료: 의존성/설정 존재(PgVector)
- 미완: 실제 상담 임베딩 write/search 파이프라인 없음

### 7. 관계 메모리 검색 + 요약
- 상태: 미완료

### 8. 팔로업 메시지 생성
- 상태: 부분 완료
- 완료: Follow-up task 조회/전송/미루기 API
- 미완: AI 기반 메시지 생성 로직은 없음

### 9. 배치 생성 및 스케줄링
- 상태: 미완료

요약: 로드맵 기준 **완료 1~4**, **부분완료 5/6/8**, **미완료 7/9**.

---

## 주요 파일(신규/핵심)

- `src/main/kotlin/com/soloboss/ai/application/task/FollowUpTaskService.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/task/TaskController.kt`
- `src/main/kotlin/com/soloboss/ai/application/stats/StatsService.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/stats/StatsController.kt`
- `src/main/kotlin/com/soloboss/ai/application/integration/duplicate/DuplicatePolicyService.kt`
- `src/main/kotlin/com/soloboss/ai/web/v1/duplicate/DuplicateController.kt`
- `src/main/kotlin/com/soloboss/ai/application/notification/AlimtalkService.kt`
- `src/main/kotlin/com/soloboss/ai/infrastructure/external/DefaultKakaoIntegrations.kt`

---

## 다음 우선 작업 제안

1. Consultation CRUD API를 명시적으로 추가해 로드맵 5 완료 처리
2. 임베딩 저장/검색/요약 유스케이스 추가로 6~7 진행
3. 팔로업 AI 초안 생성(LLM) + 발송 추천 시점 로직으로 8 완료
4. 스케줄러(배치)와 만료 처리(Review EXPIRED, Follow-up due)로 9 진행

---

## 검증

최근 검증 명령:
```bash
./gradlew compileKotlin test ktlintCheck
```
결과: 성공
