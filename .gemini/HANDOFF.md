# Solo Boss Frontend/Backend API Handoff

## Snapshot
- Updated: 2026-02-17
- Backend branch: `main`
- API base: `/api/v1`
- Frontend owner context: `NEXT_PUBLIC_OWNER_ID` (fallback `11111111-1111-1111-1111-111111111111`)

## What Was Aligned In This Session

### Frontend API client changes
- Updated `/Users/mediquitous/Desktop/project/solo-boss/frontend/lib/api.ts` to match current backend contracts.
- Updated `/Users/mediquitous/Desktop/project/solo-boss/frontend/components/ClientList.tsx` to consume real customer API.
- Added missing shared types in `/Users/mediquitous/Desktop/project/solo-boss/frontend/types/index.ts` (`MessageDraft`, `ReviewItem`).

## Current Source of Truth: Backend Endpoints

### Customer
- `POST /api/v1/customers` (body includes `ownerId`)
- `GET /api/v1/customers?ownerId={uuid}&page=...&size=...`
- `GET /api/v1/customers/{customerId}?ownerId={uuid}`
- `PATCH /api/v1/customers/{customerId}?ownerId={uuid}`
- `DELETE /api/v1/customers/{customerId}?ownerId={uuid}`

### Review
- `GET /api/v1/reviews?ownerId={uuid}&status=OPEN|IN_PROGRESS|RESOLVED|EXPIRED&page=...&size=...`
- `GET /api/v1/reviews/{reviewTaskId}?ownerId={uuid}`
- `PATCH /api/v1/reviews/{reviewTaskId}/resolve?ownerId={uuid}`
  - request body:
  ```json
  {
    "correctedPayload": {
      "resolvedValue": "..."
    }
  }
  ```

### OCR
- `POST /api/v1/ocr/extract`
- `GET /api/v1/ocr/jobs/{jobId}?ownerId={uuid}`

### Notification (Mock sender)
- `POST /api/v1/notifications/alimtalk`
  - request body:
  ```json
  {
    "templateCode": "RECEIVED_ACK",
    "to": "k_user_001",
    "variables": {
      "eta_seconds": "30",
      "source_type": "스크린샷",
      "received_at": "2026-02-17 19:20",
      "job_status_link": "https://..."
    }
  }
  ```

### Kakao Webhook
- `POST /api/v1/integrations/kakao/webhook`

## Frontend Mapping Implemented

### `/frontend/lib/api.ts`
- `getReviewItems()` -> `GET /reviews?ownerId=...&status=OPEN`
- `resolveReviewItem(id, value)` -> `PATCH /reviews/{id}/resolve?ownerId=...`
- `getClients()` -> `GET /customers?ownerId=...`
- `getClientDetail(id)` -> `GET /customers/{id}?ownerId=...`
- `sendMessage(taskId)` -> temporary fallback to `POST /notifications/alimtalk` (mock)
- `getTodayTasks()` -> temporary empty list until follow-up task API exists

## Known API Gaps (Frontend Still Blocked)

1. Today Tasks real data API missing
- Frontend wanted `/tasks`, `/tasks/{id}/send`
- Backend does not yet provide follow-up task list/send endpoints

2. Stats API missing
- Frontend wanted `/stats`
- Backend does not yet expose metrics endpoint

3. Client detail enrichment missing
- Backend customer response has no revenue/projectHistory aggregation yet

## Required Next Backend Work For Full Frontend

1. Follow-up endpoints
- `GET /api/v1/tasks?ownerId=...`
- `POST /api/v1/tasks/{taskId}/send?ownerId=...`

2. Dashboard stats endpoint
- `GET /api/v1/stats?ownerId=...`

3. Customer detail aggregate endpoint (optional)
- could keep current `GET /customers/{id}` and add dedicated history/revenue endpoint

## Required Next Frontend Work

1. Move owner id from fallback to authenticated session source.
2. Replace temporary Today tab empty state with real tasks after follow-up API lands.
3. Replace Statistics mock state with real API fetch.
4. Review resolve payload refinement:
- currently sends `{ resolvedValue }`
- should send field-keyed payload once UI edits multiple uncertain fields.

## Notes For Frontend AI Session

- Use `/Users/mediquitous/Desktop/project/solo-boss/frontend/lib/api.ts` as integration entrypoint.
- Do not reintroduce old endpoints (`/clients`, `/tasks`, `/stats`) without backend support.
- `GET /reviews` and `GET /customers` return Spring `Page` shape (`content`, `totalElements`, ...). Parse `content` first.
