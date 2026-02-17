# Gemini 프론트엔드 프롬프트: 상담 이력 UI

## 역할

너는 Next.js 14 + TypeScript + Tailwind CSS 프론트엔드 개발자야.
기존 SoloBoss AI 모바일 웹 콘솔에 **상담 이력(Consultation) UI**를 추가해야 해.

## 프로젝트 컨텍스트

- **프로젝트**: 1인 프리랜서용 AI CRM (SoloBoss AI)
- **프론트엔드 위치**: `frontend/`
- **기술 스택**: Next.js 14.2.3 (App Router), TypeScript, Tailwind CSS 3.4.1, lucide-react
- **디자인 시스템**:
  - Primary: `#643A71` (보라)
  - Secondary: `#FEC0CE` (핑크)
  - Background: `#0A0A0A`, Surface: `#1A1A1A`
  - 카드: `rounded-2xl` 또는 `rounded-3xl`, `border border-white/5`
  - 텍스트: `text-slate-100` (본문), `text-slate-400/500` (보조)
  - 호버: `hover:border-primary/30 transition-colors`
- **API Base URL**: `http://localhost:8082/api/v1`
- **Owner ID**: `11111111-1111-1111-1111-111111111111` (하드코딩 테스트용)

## 백엔드 API 스펙

### 상담 기록 API

```
POST   /api/v1/consultations                              → 201
GET    /api/v1/consultations?ownerId={uuid}&customerId={uuid}  → 200 (Page)
GET    /api/v1/consultations?ownerId={uuid}               → 200 (Page, 전체)
GET    /api/v1/consultations/{id}?ownerId={uuid}          → 200
PATCH  /api/v1/consultations/{id}?ownerId={uuid}          → 200
DELETE /api/v1/consultations/{id}?ownerId={uuid}          → 204
```

### Request/Response 타입

```typescript
// 생성 요청
interface CreateConsultationRequest {
  ownerId: string;      // UUID, 필수
  customerId: string;   // UUID, 필수
  summary: string;      // 필수
  rawText?: string;
  consultationDate?: string; // ISO 8601 OffsetDateTime
}

// 수정 요청
interface UpdateConsultationRequest {
  summary?: string;
  rawText?: string;
  consultationDate?: string;
}

// 응답
interface ConsultationResponse {
  id: string;
  ownerId: string;
  customerId: string;
  ingestJobId: string | null;
  summary: string;
  rawText: string | null;
  consultationDate: string;  // ISO 8601
  createdAt: string;
  updatedAt: string;
}

// 페이지 응답 (Spring Data 형태)
interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // 현재 페이지 (0-based)
}
```

## 구현 요구사항

### 1. TypeScript 타입 추가 (`frontend/types/index.ts`)

`ConsultationItem`과 `ConsultationDetail` 인터페이스를 추가해줘.

### 2. API 클라이언트 추가 (`frontend/lib/api.ts`)

기존 `api` 객체에 다음 메서드를 추가:

```typescript
// --- Consultations (상담 이력) ---
getConsultations(customerId?: string): Promise<ConsultationItem[]>
getConsultationDetail(id: string): Promise<ConsultationDetail>
createConsultation(data: CreateConsultationRequest): Promise<ConsultationDetail>
updateConsultation(id: string, data: UpdateConsultationRequest): Promise<ConsultationDetail>
deleteConsultation(id: string): Promise<void>
```

### 3. 고객 상세 페이지에 상담 이력 섹션 추가 (`frontend/app/clients/[id]/page.tsx`)

기존 고객 상세 페이지(`/clients/{id}`)의 "프로젝트 히스토리" 섹션 아래에 **상담 이력** 섹션을 추가:

- 해당 고객의 상담 목록을 시간순(최신 먼저)으로 표시
- 각 카드에 표시: `summary` (요약), `consultationDate` (날짜), `rawText` 존재 여부 아이콘
- 카드 클릭 시 상세 모달 또는 인라인 확장으로 `rawText` 표시
- "새 상담 기록" 버튼 → 바텀시트 또는 모달로 입력 폼 표시
  - 필드: summary (텍스트영역, 필수), rawText (텍스트영역, 선택), consultationDate (날짜 선택, 기본값: 오늘)
- 수정/삭제 기능 (스와이프 또는 메뉴 버튼)

### 4. UI/UX 가이드라인

- 기존 디자인 시스템을 **정확히** 따를 것 (위 색상, 라운딩, 간격 참고)
- 모바일 퍼스트 (max-width 제약 없음, 패딩 `p-6`)
- 빈 상태(empty state): lucide-react 아이콘 + 안내 텍스트
- 로딩 상태: `animate-pulse` 스켈레톤 또는 텍스트
- 에러 상태: 콘솔 로그 + 사용자 친화적 메시지
- 애니메이션: `animate-in fade-in slide-in-from-bottom-4 duration-500` (기존 패턴)

### 5. 참고: 기존 코드 패턴

고객 상세 페이지 패턴 (`frontend/app/clients/[id]/page.tsx`):
- `useParams()`로 ID 추출
- `useState` + `useEffect`로 데이터 페칭
- 로딩/에러/정상 3가지 상태 렌더링
- lucide-react 아이콘 사용

API 클라이언트 패턴 (`frontend/lib/api.ts`):
- `fetch()` 사용 (axios 아님)
- `OWNER_ID` 상수를 쿼리 파라미터로 전달
- `PageResponse<T>` 타입으로 페이지 응답 파싱
- 에러 시 `throw new Error()` + 설명 메시지

## 기대 결과물

1. `frontend/types/index.ts` — Consultation 타입 추가
2. `frontend/lib/api.ts` — Consultation API 메서드 추가
3. `frontend/app/clients/[id]/page.tsx` — 상담 이력 섹션 + 생성/수정/삭제 UI 추가
4. (선택) 필요시 별도 컴포넌트 파일 (`frontend/components/ConsultationList.tsx` 등)

## 제약사항

- 새 패키지 설치하지 말 것 (lucide-react, tailwindcss 등 기존 것만 사용)
- `'use client'` 디렉티브 유지
- 한국어 UI 텍스트 사용
