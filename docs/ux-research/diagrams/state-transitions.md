# State Transition Diagrams (v1)

## 1) `ingest_job`

```mermaid
stateDiagram-v2
    [*] --> RECEIVED
    RECEIVED --> OCR_DONE: OCR success
    RECEIVED --> FAILED: source/signature error
    OCR_DONE --> STRUCTURED: LLM success
    OCR_DONE --> FAILED: retry limit exceeded
    STRUCTURED --> AUTO_SAVED: confidence >= 0.85
    STRUCTURED --> NEEDS_REVIEW: confidence < 0.85
    NEEDS_REVIEW --> AUTO_SAVED: manual review confirmed
    NEEDS_REVIEW --> EXPIRED: review timeout
    AUTO_SAVED --> [*]
    FAILED --> [*]
    EXPIRED --> [*]
```

## 2) `review_task`

```mermaid
stateDiagram-v2
    [*] --> OPEN
    OPEN --> IN_PROGRESS: reviewer opened
    IN_PROGRESS --> RESOLVED: save confirmed
    IN_PROGRESS --> OPEN: temporary leave
    OPEN --> EXPIRED: due time passed
    RESOLVED --> [*]
    EXPIRED --> [*]
```

## 3) `follow_up_task`

```mermaid
stateDiagram-v2
    [*] --> SCHEDULED
    SCHEDULED --> DRAFT_READY: draft generated
    DRAFT_READY --> SENT: send now
    DRAFT_READY --> EDITING: edit draft
    EDITING --> SENT: save and send
    DRAFT_READY --> SNOOZED: snooze 1 day
    SNOOZED --> DRAFT_READY: becomes due again
    DRAFT_READY --> CANCELED: no longer needed
    SENT --> [*]
    CANCELED --> [*]
```

## Operational Notes

- 재시도는 워커 레벨에서 지수 백오프로 처리하고, 한계 초과 시 `FAILED`로 종료한다.
- `NEEDS_REVIEW`는 모바일 웹 콘솔의 `검수함`과 1:1 대응한다.
- `DRAFT_READY`에서 `SNOOZED` 전이는 카카오 알림톡 CTA(`내일로 미루기`)와 직접 연결된다.
