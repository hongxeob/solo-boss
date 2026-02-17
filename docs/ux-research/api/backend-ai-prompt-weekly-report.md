# Backend AI Prompt - Weekly Coaching Report Implementation

아래 프롬프트를 백엔드 AI에게 그대로 전달해서 구현 작업을 요청하면 된다.

```text
You are implementing SoloBoss AI weekly coaching report backend in Kotlin + Spring Boot 3.4.

Context:
- Core spec: /Users/mediquitous/Desktop/project/solo-boss/docs/ux-research/api/weekly-coaching-report-events.md
- OpenAPI contract: /Users/mediquitous/Desktop/project/solo-boss/docs/ux-research/api/openapi-weekly-coaching-report.yaml
- Existing ingestion patterns: /Users/mediquitous/Desktop/project/solo-boss/docs/ux-research/api/kakao-ingestion-events.md

Goal:
Implement the weekly coaching report pipeline that runs every Monday 08:30 Asia/Seoul and sends AlimTalk reports.

Requirements:
1) Add scheduler job `weekly_coaching_report_job` (KST 08:30 Monday, idempotent by `weekly_report:{owner_id}:{iso_week}`).
2) Implement API endpoints defined in OpenAPI:
   - POST /api/v1/reports/weekly/run
   - POST /api/v1/reports/weekly/metrics
   - POST /api/v1/reports/weekly/priorities
   - POST /api/v1/reports/weekly/render
   - POST /api/v1/reports/weekly/archive
   - PATCH /api/v1/reports/weekly/jobs/{jobId}/status
3) Add domain models + persistence for:
   - weekly_report_job
   - weekly_report_archive
4) Status transitions:
   - SCHEDULED -> CALCULATING -> RENDERED -> SENT -> ARCHIVED
   - failures: FAILED_METRICS, FAILED_SEND, RETRYING, FAILED_FINAL
5) Retry policy:
   - on send failure retry twice with 15-minute interval
   - after final failure archive report and set FAILED_FINAL
6) Rendering:
   - voice_profile = COACH
   - include estimated labels for low-confidence probabilities
7) Add tests:
   - scheduler trigger + idempotency
   - priority ranking logic
   - retry/final-failure behavior
   - status transition validation
8) Keep code style aligned with existing project conventions.

Output format:
- First provide implementation plan with file-level changes.
- Then implement.
- Finally report:
  - changed files
  - test commands run
  - test results
  - unresolved risks (if any)
```
