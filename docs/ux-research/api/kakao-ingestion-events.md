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
