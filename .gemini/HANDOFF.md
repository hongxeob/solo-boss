# 프로젝트 핸드오프: Solo Boss (전문직 프리랜서 비즈니스 케어)

## 1. 프로젝트 개요
- **타겟**: 전문직 프리랜서 (디자이너, 개발자, 자문 등)
- **핵심 가치**: "신뢰도 기반의 비즈니스 관리". AI가 상담 기록을 대신 읽고(OCR), 불확실한 정보는 사용자에게 검수를 요청하며, 팔로업 업무를 자동화함.

## 2. 주요 기술 스택
- **Backend (Spec)**: Spring AI + Google Gemini 1.5 Pro/Flash
- **Frontend**: Next.js (App Router), Tailwind CSS, TypeScript
- **Design**: 세련된 다크 모드 (Premium Dark UI)

## 3. 작업 완료 사항

### 3.1 AI 전략 및 스펙 (`/docs/ai-spec/`)
- `OCR_EXTRACTION_GUIDE.md`: Gemini Multimodal을 활용한 일반 OCR 추출 가이드.
- `FREELANCER_NOTE_EXTRACTION.md`: **핵심 로직**. 각 추출 필드(고객명, 예산, 기한 등)에 대해 0.0~1.0 사이의 `confidence`(신뢰도) 점수를 반환하도록 설계된 프롬프트 전략.

### 3.2 프론트엔드 프로토타입 (`/frontend/`)
- **3탭 구조 구현**:
  1. **오늘 할 일 (`TodayTasks`)**: AI가 생성한 메시지 초안 확인 및 즉시 전송.
  2. **검수함 (`ReviewBox`)**: 신뢰도가 낮은 데이터를 시각화(Red/Amber)하여 사용자가 수정할 수 있게 함.
  3. **고객 리스트 (`ClientList`)**: 관리 중인 고객 목록 조회.
- **환경 설정**: 인텔리제이 개발 환경을 위한 `package.json`, `tsconfig.json` 구성 완료.

## 4. 다음 세션 권장 작업 (Next Steps)

1. **Frontend 의존성 설치 및 실행**:
   - `cd frontend && npm install` 실행.
   - `npm run dev`로 실제 화면 구동 및 UI 디테일 조정.
2. **Spring AI 백엔드 실제 구현**:
   - `docs/ai-spec`에 정의된 규격을 바탕으로 실제 Spring Boot 프로젝트 생성 및 Gemini API 연동.
   - `BeanOutputConverter`를 사용해 `ConsultationExtraction` 레코드 형태로 데이터 파싱 구현.
3. **이미지 업로드 기능**:
   - 프론트엔드에서 상담 메모 사진을 찍어 백엔드로 전송하는 API 연결.

## 5. 핵심 결정 사항 (Context)
- **왜 다크 모드인가?**: 전문직 프리랜서의 세련된 이미지와 몰입감 있는 작업 환경을 위해 다크 테마를 기본으로 채택함.
- **왜 신뢰도 점수인가?**: AI의 실수를 사용자에게 투명하게 공개함으로써 시스템 전체의 신뢰도를 높이는 '신뢰도 기반 UX' 전략의 핵심임.
