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

### 3.2 프론트엔드 프로토타입 및 연동 (`/frontend/`)
- **4탭 구조 확장**: 
  1. **오늘 할 일 (`TodayTasks`)**: API 연동을 통해 AI 초안 확인 및 즉시 전송 로직 구현.
  2. **검수함 (`ReviewBox`)**: 신뢰도 50% 미만 항목 붉은색 강조 UI, 인라인 수정 모드 및 `resolve` API 연동 완료.
  3. **고객 리스트 (`ClientList` & `ClientDetailView`)**: 리스트 조회 및 상세 정보(매출 히스토리, 연락처 등) 보기 기능 추가.
  4. **리포트 (`Statistics`)**: 월별 수익 추이 및 AI 효율(절약 시간, 정확도) 시각화 대시보드 구현.
- **API 레이어 (`lib/api.ts`)**: `/api/v1` 규격에 맞춘 중앙 집중형 API 서비스 구축 및 Next.js API Routes를 이용한 Mock 서버 구성 완료.

## 4. 다음 세션 권장 작업 (Next Steps)

1. **Spring AI 백엔드 실구현**:
   - `docs/ai-spec` 규격에 맞춰 `/api/v1/reviews` 및 `/api/v1/tasks` 엔드포인트 실제 구현.
   - `BeanOutputConverter`를 사용하여 이미지에서 신뢰도 점수가 포함된 JSON 파싱.
2. **상세 페이지 라우팅 고도화**:
   - 현재 `page.tsx` 내 상태값으로 관리되는 상세 보기를 Next.js Dynamic Routes(`[id]/page.tsx`)로 전환 검토.
3. **이미지 업로드 Flow**:
   - 카메라/갤러리 접근 후 이미지를 백엔드로 전송하고 결과를 받는 전체 파이프라인 연결.

## 5. 핵심 결정 사항 (Context)
- **왜 다크 모드인가?**: 전문직 프리랜서의 세련된 이미지와 몰입감 있는 작업 환경을 위해 다크 테마를 기본으로 채택함.
- **왜 신뢰도 점수인가?**: AI의 실수를 사용자에게 투명하게 공개함으로써 시스템 전체의 신뢰도를 높이는 '신뢰도 기반 UX' 전략의 핵심임.
