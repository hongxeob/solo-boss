# SoloBoss AI UX Research Pack

이 패키지는 `SoloBoss AI`의 카카오톡 중심 UX 기준 문서 모음이다.
목적은 사람/AI 에이전트가 동일한 기준으로 기획, 구현, 테스트, 운영 문구를 참조하게 하는 것이다.

## Scope

- 입력: 카카오톡 채널로 고객 상담 스크린샷/음성 수신
- 처리: OCR + LLM 구조화 + 신뢰도 기반 자동 저장/검수 분기
- 출력: 최소 모바일 웹 콘솔 + 카카오 알림톡

## Documents

- `notifications/alimtalk-templates.md`
  - 접수 확인, 처리 완료(자동/검수), 팔로업 리마인드 템플릿 카피
- `api/kakao-ingestion-events.md`
  - 카카오 Webhook부터 최종 저장까지 API 호출 순서와 요청 파라미터
- `api/weekly-coaching-report-events.md`
  - 월요일 주간 코칭 리포트 스케줄러/이벤트/API 명세
- `api/openapi-weekly-coaching-report.yaml`
  - 주간 코칭 리포트 OpenAPI 3.1 계약 초안
- `api/backend-ai-prompt-weekly-report.md`
  - 백엔드 AI 구현 요청용 프롬프트 템플릿
- `diagrams/state-transitions.md`
  - `ingest_job`, `review_task`, `follow_up_task` 상태 전이 다이어그램
- `retention-weekly-report-and-persona.md`
  - 리텐션 전략, 월요일 주간 리포트 설계, 에러 페르소나 보이스/톤 가이드

## Product Constants (v1)

- 자동 저장 임계치: `overall_confidence >= 0.85`
- 조건부/전체 검수: `overall_confidence < 0.85`
- 멱등키: `channel_id:message_id`
- 저장 시간 기준: DB `UTC`, UX 표시는 `Asia/Seoul`

## Intended Consumers

- 백엔드 AI 에이전트: 이벤트 순서/파라미터/상태 전이 기준 참조
- 프론트엔드 AI 에이전트: 검수함/고객카드/팔로업 UX 메시지 참조
- PM/디자인 에이전트: 카피 톤과 사용자 흐름 일관성 검증
