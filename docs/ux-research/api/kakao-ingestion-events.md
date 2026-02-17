# Kakao Ingestion API Event Spec (v1)

목표: 카카오 Webhook 유입부터 최종 저장 상태 업데이트까지 호출 순서와 요청 파라미터를 고정한다.

## Sequence

| # | Caller -> Callee | Method/Endpoint | Purpose |
|---|---|---|---|
| 1 | Kakao -> SoloBoss | `POST /api/v1/integrations/kakao/webhook` | 카카오 이벤트 수신 |
| 2 | Webhook Handler -> Ingest | `POST /api/v1/ingest/jobs` | 수신 건을 처리 작업으로 등록 |
| 3 | Ingest -> Notification | `POST /api/v1/notifications/alimtalk` | 접수 확인 발송 |
| 4 | Worker -> OCR | `POST /api/v1/ai/ocr/extract` | 텍스트 추출 |
| 5 | Worker -> LLM Structuring | `POST /api/v1/ai/crm/structure` | CRM 스키마 구조화 |
| 6 | Worker -> Confidence | `POST /api/v1/ai/confidence/score` | 신뢰도 산정 |
| 7A | Worker -> CRM | `POST /api/v1/crm/conversations/auto-save` | 자동 저장 분기 |
| 8A | Worker -> Notification | `POST /api/v1/notifications/alimtalk` | 자동 저장 완료 발송 |
| 7B | Worker -> Review | `POST /api/v1/reviews` | 검수 필요 분기 |
| 8B | Worker -> Notification | `POST /api/v1/notifications/alimtalk` | 검수 요청 발송 |
| 9 | Worker -> Ingest | `PATCH /api/v1/ingest/jobs/{job_id}/status` | 최종 상태 반영 |

## Request Parameters

## 1) `POST /api/v1/integrations/kakao/webhook`

```json
{
  "event_id": "evt_20260217_001",
  "message_id": "msg_abc123",
  "channel_id": "ch_001",
  "kakao_user_key": "k_user_001",
  "message_type": "image",
  "media_url": "https://...",
  "sent_at": "2026-02-17T06:21:20Z",
  "signature": "kakao-signature",
  "raw_payload": {}
}
```

## 2) `POST /api/v1/ingest/jobs`

```json
{
  "event_id": "evt_20260217_001",
  "message_id": "msg_abc123",
  "owner_id": "owner_001",
  "source_type": "image",
  "source_url": "s3://bucket/path/file.png",
  "kakao_user_key": "k_user_001",
  "received_at": "2026-02-17T06:21:25Z",
  "idempotency_key": "ch_001:msg_abc123"
}
```

## 3) `POST /api/v1/notifications/alimtalk` (접수 확인)

```json
{
  "template_code": "RECEIVED_ACK",
  "to": "k_user_001",
  "variables": {
    "eta_seconds": 30,
    "source_type": "스크린샷",
    "received_at": "2026-02-17 15:21",
    "job_status_link": "https://..."
  }
}
```

## 4) `POST /api/v1/ai/ocr/extract`

```json
{
  "job_id": "job_001",
  "source_url": "s3://bucket/path/file.png",
  "source_type": "image",
  "lang": "ko",
  "ocr_mode": "conversation"
}
```

## 5) `POST /api/v1/ai/crm/structure`

```json
{
  "job_id": "job_001",
  "ocr_text": "추출된 텍스트",
  "schema_version": "v1",
  "timezone": "Asia/Seoul",
  "owner_context": {
    "business_type": "one-person business"
  }
}
```

## 6) `POST /api/v1/ai/confidence/score`

```json
{
  "job_id": "job_001",
  "extracted_fields": {
    "customer_name": "홍길동",
    "phone": "010-0000-0000",
    "summary": "상담 핵심 요약"
  },
  "threshold_profile": "default_smb"
}
```

## 7A) `POST /api/v1/crm/conversations/auto-save`

```json
{
  "job_id": "job_001",
  "customer_candidate": {
    "name": "홍길동",
    "phone": "010-0000-0000",
    "kakao_user_key": "k_user_001"
  },
  "conversation": {
    "happened_at": "2026-02-17T06:20:00Z",
    "summary": "상담 요약",
    "raw_text": "원문 텍스트"
  },
  "followup": {
    "due_at": "2026-02-20T01:00:00Z",
    "objective": "견적 재안내",
    "draft_seed": "친근하고 간결한 톤"
  },
  "confidence": {
    "overall": 0.91
  }
}
```

## 7B) `POST /api/v1/reviews`

```json
{
  "job_id": "job_001",
  "customer_guess": "홍길동",
  "uncertain_fields": ["customer_name", "followup_due_at"],
  "proposed_payload": {},
  "confidence": {
    "overall": 0.72
  },
  "expires_at": "2026-02-18T06:21:25Z"
}
```

## 9) `PATCH /api/v1/ingest/jobs/{job_id}/status`

```json
{
  "status": "AUTO_SAVED",
  "confidence": 0.91,
  "error_reason": null,
  "processed_at": "2026-02-17T06:21:55Z"
}
```

## Processing Rules

- 신뢰도 분기:
  - `overall_confidence >= 0.85` -> `AUTO_SAVED`
  - `overall_confidence < 0.85` -> `NEEDS_REVIEW`
- 멱등 처리:
  - `idempotency_key(channel_id:message_id)` unique
- 검증:
  - 카카오 시그니처 실패 시 `401 Unauthorized`
- 시간 처리:
  - 저장 `UTC`, 사용자 노출 `Asia/Seoul`

## Notification Template Mapping (Status/Error -> Template)

| Category | Internal Status/Error Code | Template Code | Trigger Point | Required Variables (example) |
|---|---|---|---|---|
| 접수 확인 | `RECEIVED` | `RECEIVED_ACK` | `ingest_job` 생성 직후 | `eta_seconds`, `source_type`, `received_at`, `job_status_link` |
| 처리 완료(자동) | `AUTO_SAVED` | `PROCESS_AUTO_DONE` | 자동 저장 성공 직후 | `customer_name`, `summary_one_line`, `followup_at`, `customer_card_link` |
| 처리 완료(검수 필요) | `NEEDS_REVIEW` | `PROCESS_REVIEW_REQUIRED` | review task 생성 직후 | `review_count`, `customer_guess`, `uncertain_fields`, `review_link` |
| 팔로업 리마인드 | `FOLLOW_UP_DUE` | `FOLLOWUP_REMINDER` | 팔로업 스케줄 도달 시점 | `customer_name`, `followup_objective`, `recommended_send_at`, `draft_message` |
| 입력 오류(텍스트만) | `ERR_TEXT_ONLY` | `OCR_TEXT_ONLY` | webhook 파싱 후 미디어 없음 | `received_at` |
| 입력 오류(흐린 이미지) | `ERR_IMAGE_BLURRY` | `OCR_IMAGE_BLURRY` | OCR 품질 점수 임계 미달 | `quality_score`, `retry_hint` |
| 입력 오류(노출 불량) | `ERR_IMAGE_EXPOSURE` | `OCR_IMAGE_EXPOSURE` | OCR 전처리 노출 실패 | `retry_hint` |
| 입력 오류(상담 아님) | `ERR_NOT_CONVERSATION` | `OCR_NOT_CONVERSATION` | 콘텐츠 분류 결과 대화 아님 | `source_type` |
| 입력 오류(다중 순서 불명확) | `ERR_MULTI_IMAGE_ORDER` | `OCR_MULTI_IMAGE_ORDER` | 다중 파일 문맥 결합 실패 | `image_count`, `retry_hint` |
| 중복 입력 감지(병합) | `DUPLICATE_MERGED` | `DUPLICATE_MERGED_NOTICE` | 멱등/준중복 병합 직후 | `customer_name`, `merged_at`, `undo_link` |

## Error/Duplicate Branch Notes

- `ERR_*` 계열은 `AUTO_SAVED/NEEDS_REVIEW` 분기 이전에 종료될 수 있다.
- `DUPLICATE_MERGED`는 새 레코드를 만들지 않고 기존 레코드에 병합한 성공 케이스다.
- `DUPLICATE_MERGED_NOTICE`는 1회 발송 원칙을 적용한다(동일 `idempotency_key` 재발송 금지).
- 템플릿 원문 카피 기준:
  - `/Users/mediquitous/Desktop/project/solo-boss/docs/ux-research/notifications/alimtalk-templates.md`
