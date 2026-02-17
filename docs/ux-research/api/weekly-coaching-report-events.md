# Weekly Coaching Report API/Event Spec (v1)

목표: 매주 월요일 `08:30 KST`에 사용자별 영업 코칭 리포트를 생성/발송하는 이벤트 흐름을 고정한다.

## 1) Scheduler Job Contract

- Job Name: `weekly_coaching_report_job`
- Cron (KST): `0 30 8 ? * MON`
- Timezone: `Asia/Seoul`
- Scope: `ACTIVE` 사용자 전체
- Idempotency Key:
  - `weekly_report:{owner_id}:{iso_week}`

## 2) End-to-End Sequence

| # | Caller -> Callee | Method / Endpoint | Purpose |
|---|---|---|---|
| 1 | Scheduler -> Report Orchestrator | `POST /api/v1/reports/weekly/run` | 주간 리포트 배치 시작 |
| 2 | Orchestrator -> Metrics Service | `POST /api/v1/reports/weekly/metrics` | 지난주 상담/매출 지표 계산 |
| 3 | Orchestrator -> Scoring Service | `POST /api/v1/reports/weekly/priorities` | 이번 주 연락 우선순위 1~3 산정 |
| 4 | Orchestrator -> Template Builder | `POST /api/v1/reports/weekly/render` | 카톡 본문/변수 렌더링 |
| 5 | Orchestrator -> Notification | `POST /api/v1/notifications/alimtalk` | 주간 코칭 리포트 발송 |
| 6 | Orchestrator -> Report Store | `POST /api/v1/reports/weekly/archive` | 발송본 저장(콘솔 조회용) |
| 7 | Orchestrator -> Job Store | `PATCH /api/v1/reports/weekly/jobs/{job_id}/status` | 성공/실패 상태 반영 |

## 3) Request Schemas

## 1) `POST /api/v1/reports/weekly/run`

```json
{
  "run_at": "2026-02-23T08:30:00+09:00",
  "timezone": "Asia/Seoul",
  "target_owner_ids": ["owner_001"],
  "force_rerun": false
}
```

## 2) `POST /api/v1/reports/weekly/metrics`

```json
{
  "owner_id": "owner_001",
  "period": {
    "from": "2026-02-16T00:00:00+09:00",
    "to": "2026-02-22T23:59:59+09:00"
  }
}
```

### metrics response example

```json
{
  "consultation_count": 5,
  "expected_revenue": 5000000,
  "expected_revenue_delta_value": 760000,
  "expected_revenue_delta_rate": 0.18
}
```

## 3) `POST /api/v1/reports/weekly/priorities`

```json
{
  "owner_id": "owner_001",
  "as_of": "2026-02-23T08:30:00+09:00",
  "max_items": 3
}
```

### priorities response example

```json
{
  "items": [
    {
      "customer_id": "c_101",
      "customer_masked_name": "박OO",
      "reason": "3일 무응답",
      "win_probability": 0.80,
      "is_estimated": true,
      "priority_score": 0.624
    }
  ]
}
```

## 4) `POST /api/v1/reports/weekly/render`

```json
{
  "owner_id": "owner_001",
  "voice_profile": "COACH",
  "metrics": {},
  "priority_items": []
}
```

### render response example

```json
{
  "template_code": "WEEKLY_COACHING_REPORT",
  "variables": {
    "consultation_count": 5,
    "expected_revenue_krw": 5000000,
    "delta_rate_pct": 18,
    "top1_name": "박OO",
    "top1_prob_pct": 80,
    "top1_is_estimated": true
  },
  "message_preview": "[이번 주 영업 코칭 리포트] ..."
}
```

## 5) `POST /api/v1/notifications/alimtalk`

```json
{
  "template_code": "WEEKLY_COACHING_REPORT",
  "to": "k_user_001",
  "variables": {
    "consultation_count": 5,
    "expected_revenue_krw": 5000000,
    "delta_rate_pct": 18
  }
}
```

## 4) State Model

`weekly_report_job.status`

- `SCHEDULED` -> `CALCULATING` -> `RENDERED` -> `SENT` -> `ARCHIVED`
- 실패 분기:
  - `CALCULATING` -> `FAILED_METRICS`
  - `RENDERED` -> `FAILED_SEND`
- 재시도:
  - `FAILED_SEND` 상태에서 `RETRYING` -> `SENT` 또는 `FAILED_FINAL`

## 5) Retry and Fallback Rules

- 기본 재시도: 15분 간격 2회
- 3회 모두 실패 시:
  - `FAILED_FINAL`로 종료
  - 리포트 본문을 웹 콘솔 `주간 리포트함`에 저장
  - 알림 채널 장애 플래그 기록

## 6) Template Mapping

| Internal Code | Template Code | Description |
|---|---|---|
| `WEEKLY_REPORT_READY` | `WEEKLY_COACHING_REPORT` | 월요일 주간 코칭 리포트 본문 |
| `WEEKLY_REPORT_FALLBACK` | `WEEKLY_REPORT_WEBBOX_NOTICE` | 카톡 발송 실패 시 웹 콘솔 확인 안내 |

## 7) Derived Metric Rules

- 예상매출:
  - `expected_revenue = Σ(opportunity_amount * win_probability)`
- 우선순위 점수:
  - `priority_score = win_probability * potential_amount * urgency_decay`
- 추정치 라벨 부착:
  - 근거 데이터 부족 또는 최근 상호작용 부재 시 `is_estimated=true`

## 8) OpenAPI Skeleton (Implementation Hint)

```yaml
paths:
  /api/v1/reports/weekly/run:
    post:
      summary: Run weekly coaching report batch
  /api/v1/reports/weekly/metrics:
    post:
      summary: Calculate weekly performance metrics
  /api/v1/reports/weekly/priorities:
    post:
      summary: Rank top follow-up targets
  /api/v1/reports/weekly/render:
    post:
      summary: Render coach-style AlimTalk payload
  /api/v1/reports/weekly/archive:
    post:
      summary: Persist report snapshot for web console
  /api/v1/reports/weekly/jobs/{job_id}/status:
    patch:
      summary: Update weekly report job status
```
