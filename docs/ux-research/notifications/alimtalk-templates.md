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

## 5) 입력 오류/품질 이슈 안내 (재치형)

## 5-1) 텍스트만 수신됨

- 템플릿 코드: `OCR_TEXT_ONLY`
- 제목: `[SoloBoss AI] 캡처/음성이 필요해요`
- 본문:

```text
앗, 글만 도착했어요 🙏
저는 지금 상담 캡처/음성 정리에 특화돼 있어요.
캡처 1장(또는 음성 1개)만 다시 보내주시면 바로 정리할게요!
```

## 5-2) 이미지가 매우 흐림

- 템플릿 코드: `OCR_IMAGE_BLURRY`
- 제목: `[SoloBoss AI] 이미지가 흐려요`
- 본문:

```text
이 사진은 제 눈엔 안개 낀 월요일 같아요 🌫️
글자가 잘 안 보여요.
화면을 조금 확대해서 선명하게 한 장만 다시 보내주세요!
```

## 5-3) 노출 과다(너무 밝거나 어두움)

- 템플릿 코드: `OCR_IMAGE_EXPOSURE`
- 제목: `[SoloBoss AI] 밝기 조정이 필요해요`
- 본문:

```text
노출이 강해서 텍스트가 숨어버렸어요 😵
밝기만 살짝 조정해서 다시 보내주시면,
이번엔 정확히 읽어볼게요.
```

## 5-4) 상담 캡처가 아닌 일반 사진

- 템플릿 코드: `OCR_NOT_CONVERSATION`
- 제목: `[SoloBoss AI] 상담 캡처로 다시 보내주세요`
- 본문:

```text
멋진 사진인데 상담 내용은 찾기 어려웠어요 📷
카톡 대화가 보이는 캡처로 보내주시면
고객 기록으로 바로 등록해드릴게요.
```

## 5-5) 다중 이미지로 순서/품질 불명확

- 템플릿 코드: `OCR_MULTI_IMAGE_ORDER`
- 제목: `[SoloBoss AI] 순서가 살짝 꼬였어요`
- 본문:

```text
자료가 한꺼번에 와서 순서가 살짝 꼬였어요 🌀
가장 중요한 캡처 1장부터 보내주시면,
나머지도 순서대로 깔끔하게 정리해드릴게요.
```

## Duplicate Input UX Guideline

1. 기본 전략은 차단보다 병합이다. 같은 상담으로 판단되면 새 건 생성 대신 기존 기록에 합친다.
2. 완전 중복(`channel_id + message_id` 동일)은 저장 스킵 후 1회 안내한다.
3. 준중복(해시/시간/고객키 유사)은 `합치기(권장)` / `새로 저장` 2옵션으로 가볍게 확인한다.
4. 사용자 알림은 짧고 단일 행동 중심으로 구성한다.
5. 병합 이후 24시간 `되돌리기(Undo)`를 제공한다.
6. 고객 타임라인에 `중복 감지로 병합됨` 이벤트를 남겨 변경 이력을 투명하게 유지한다.
7. 반복 중복 발생 시 재전송 타이밍 가이드를 추가한다.
8. 중복 감지 안내 템플릿은 아래 문구를 기본으로 사용한다.

```text
같은 상담으로 보여 기존 기록에 합쳤어요.
필요하면 방금 건을 새 기록으로 다시 저장할 수 있어요.
```

## Placeholder Reference

- `{eta_seconds}`: 예상 처리 시간(초)
- `{source_type}`: `스크린샷` | `음성`
- `{received_at}`: 사용자 로컬 시각 문자열
- `{followup_at}`: 예약 팔로업 시각
- `{uncertain_fields}`: 콤마 구분 검수 대상 필드명
