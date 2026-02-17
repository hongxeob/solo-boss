# Solo Boss 프론트엔드/백엔드 API 핸드오프

## 스냅샷
- 업데이트 날짜: 2026-02-17
- 백엔드 브랜치: `main` (Port: 8082)
- 프론트엔드 브랜치: `main` (Port: 3000)
- API 베이스: `http://localhost:8082/api/v1`
- 고정 사용자 ID: `11111111-1111-1111-1111-111111111111` (테스트용)

## 이번 세션 주요 정렬 사항

### 🎨 브랜드 디자인 시스템 적용
- **키컬러**: `#643A71` (Primary - 버튼, 강조 아이콘)
- **서브컬러**: `#FEC0CE` (Secondary - 신뢰도 경고, 강조 텍스트)
- **테마**: 프리미엄 다크 UI (배경: `#0A0A0A`, 카드: `#1A1A1A`)
- **인프라**: 누락되었던 `tailwind.config.ts`, `postcss.config.js`, `layout.tsx`, `globals.css`를 구축하여 스타일 시스템 정상화 완료.

### 🚀 프론트엔드 기능 고도화
- **Dynamic Routing**: 고객 상세 페이지를 `/clients/[id]` 경로로 분리하여 내비게이션 환경 개선.
- **신뢰도 기반 UX**: 검수함(`ReviewBox`)에서 AI 확신도 50% 미만 항목에 서브컬러 강조 및 애니메이션 효과 적용.
- **탭 렌더링 수정**: '리포트' 탭 클릭 시 화면이 비어있고 API가 호출되지 않던 렌더링 로직 수정 완료.

### 🔗 실데이터 API 연결 완료
- **오늘 할 일 (`TodayTasks`)**: `/api/v1/tasks` 연결 (목록 조회, 전송, 미루기 액션 포함).
- **비즈니스 리포트 (`Statistics`)**: `/api/v1/stats` 연결 (월 매출, 프로젝트 수, AI 효율 대시보드 시각화).
- **고객 리스트**: Spring Data JPA `Page` 규격에 맞춰 페이징 파라미터(`page`, `size`) 추가 및 데이터 매핑 최적화.

## 현재 백엔드 API 명세 (Source of Truth)

### 고객 관리 (Customer)
- `GET /api/v1/customers?ownerId={uuid}&page=0&size=50`
- `GET /api/v1/customers/{id}?ownerId={uuid}`
- `POST /api/v1/customers` (Body: `CreateCustomerRequest`)

### 업무 관리 (Task)
- `GET /api/v1/tasks?ownerId={uuid}`: 오늘 할 일 목록
- `POST /api/v1/tasks/{id}/send?ownerId={uuid}`: 팔로업 메시지 즉시 전송
- `POST /api/v1/tasks/{id}/snooze?ownerId={uuid}`: 업무 미루기

### 통계 (Stats)
- `GET /api/v1/stats?ownerId={uuid}`: 이번 달 수익 및 AI 자동화 성과 지표

### 검수함 (Review)
- `GET /api/v1/reviews?ownerId={uuid}&status=OPEN`: 검수 필요 항목
- `PATCH /api/v1/reviews/{id}/resolve?ownerId={uuid}`: 검수 완료 처리

## 프론트엔드 향후 과제

1.  **이미지 업로드 Flow 구현**: 카메라/갤러리 접근 후 `/api/v1/ocr/extract`로 이미지를 전송하는 로직 추가 필요.
2.  **인증 시스템 통합**: 현재 고정된 `OWNER_ID`를 실제 로그인한 사용자의 세션 정보에서 가져오도록 수정.
3.  **고객 상세 정보 보강**: 백엔드 응답에 '매출 히스토리' 및 '프로젝트 내역'이 추가될 경우 UI 연결 필요.
4.  **에러 핸들링 고도화**: 네트워크 단절이나 API 에러 발생 시 사용자 친화적인 Toast 알림 시스템 도입.

## 개발 참고 노트
- 모든 API 호출은 `frontend/lib/api.ts`를 통합니다.
- 백엔드가 8082 포트에서 실행 중이어야 하며, `localhost:3000`에 대한 **CORS 허용** 설정이 필수입니다.
- 새로운 컴포넌트 생성 시 지정된 브랜드 컬러 변수(`bg-primary`, `text-secondary` 등)를 우선 사용하세요.
