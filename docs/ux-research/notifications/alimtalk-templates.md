# AlimTalk Templates (v1)

## 1) 접수 확인 (즉시 발송)

- 템플릿 코드: `RECEIVED_ACK`
- 제목: `[SoloBoss AI] 상담 자료 접수 완료`
- 본문:

```text
사장님, 상담 자료를 접수했어요.
지금 AI가 내용을 정리 중입니다. (보통 {eta_seconds}초)

- 자료형: {source_type}
- 접수 시각: {received_at}
- 고객 채널: 카카오톡

정리되면 바로 알려드릴게요.
```

- 버튼:
  - `처리현황 보기` -> `{job_status_link}`

## 2) 처리 완료 - 자동 저장

- 템플릿 코드: `PROCESS_AUTO_DONE`
- 제목: `[SoloBoss AI] 자동 저장 완료`
- 본문:

```text
정리 완료했습니다. 고객 기록에 자동 반영했어요.

- 고객: {customer_name}
- 핵심 요약: {summary_one_line}
- 다음 팔로업: {followup_at}

필요하면 메시지 초안을 바로 확인해보세요.
```

- 버튼:
  - `고객 카드 보기` -> `{customer_card_link}`
  - `팔로업 초안 보기` -> `{followup_draft_link}`

## 3) 처리 완료 - 검수 필요

- 템플릿 코드: `PROCESS_REVIEW_REQUIRED`
- 제목: `[SoloBoss AI] 확인 후 저장 필요`
- 본문:

```text
거의 다 정리됐어요. 아래 {review_count}개만 확인하면 저장 완료됩니다.

- 추정 고객: {customer_guess}
- 확인 항목: {uncertain_fields}
- 임시 요약: {summary_one_line}

1분만 확인해 주세요.
```

- 버튼:
  - `지금 확인하기` -> `{review_link}`

## 4) 팔로업 리마인드

- 템플릿 코드: `FOLLOWUP_REMINDER`
- 제목: `[SoloBoss AI] 팔로업 리마인드`
- 본문:

```text
팔로업 시간입니다.

- 고객: {customer_name}
- 목적: {followup_objective}
- 권장 발송 시각: {recommended_send_at}

[초안]
{draft_message}
```

- 버튼:
  - `바로 전송` -> `{send_now_link}`
  - `초안 수정` -> `{edit_draft_link}`
  - `내일로 미루기` -> `{snooze_1d_link}`

## Placeholder Reference

- `{eta_seconds}`: 예상 처리 시간(초)
- `{source_type}`: `스크린샷` | `음성`
- `{received_at}`: 사용자 로컬 시각 문자열
- `{followup_at}`: 예약 팔로업 시각
- `{uncertain_fields}`: 콤마 구분 검수 대상 필드명
