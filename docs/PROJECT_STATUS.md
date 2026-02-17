# SoloBoss AI — 프로젝트 상태 점검

> 최종 업데이트: 2026-02-17
> 기준 커밋: `7e227b1` feat: add consultation embedding and relationship memory search/summarize

---

## 1. 로드맵 진행 상황

| # | 단계 | 상태 | 커밋 | 비고 |
|---|------|------|------|------|
| 1 | 프로젝트 스캐폴딩 + CLAUDE.md | ✅ 완료 | `b292b75` | Gradle, Docker, Flyway, Spring Boot |
| 2 | 전체 도메인 모델 (엔티티, Flyway V1~V6) | ✅ 완료 | `0af2e85` | 5 엔티티, 3 상태머신, 6 마이그레이션 |
| 3 | Customer CRUD | ✅ 완료 | `0af2e85` | Service + Controller + DTO |
| 4 | 스크린샷 OCR 추출 | ✅ 완료 | `0af2e85` | Claude Vision + 2단계 프로세스 |
| 5 | Interaction CRUD (상담 기록) | ✅ 완료 | `6946a39` | ConsultationService + Controller |
| 6 | 상담 임베딩 + 벡터 저장소 | ✅ 완료 | `7e227b1` | ConsultationEmbeddingService + embedSafely 훅 |
| 7 | 관계 메모리 검색 + 요약 | ✅ 완료 | `7e227b1` | RelationshipMemoryService + MemoryController |
| 8 | 팔로업 메시지 생성 | ✅ 완료 | `5a9033d` | FollowUpTaskService + AlimtalkService |
| 9 | 배치 생성 및 스케줄링 | ✅ 완료 | `59799bb` | Webhook + 중복감지 + 알림 |

**로드맵 9단계 모두 구현 완료.**

---

## 2. 핵심 기능별 점검

### 2.1 백엔드 기능 매트릭스

| 기능 | 구현 | 테스트 | API 엔드포인트 | 비고 |
|------|:----:|:------:|---------------|------|
| 고객 CRUD | ✅ | ✅ | `GET/POST/PATCH/DELETE /api/v1/customers` | CustomerServiceTest |
| 스크린샷 OCR | ✅ | ✅ | `POST /api/v1/ocr/extract` | OcrExtractionServiceTest 외 3개 |
| 상담 CRUD | ✅ | ❌ | `GET/POST/PATCH/DELETE /api/v1/consultations` | **전용 테스트 없음** |
| 상담 임베딩 | ✅ | ❌ | 내부 (CRUD 훅) | **테스트 없음** |
| 메모리 검색/요약 | ✅ | ❌ | `POST /api/v1/memory/search`, `POST /api/v1/memory/summarize` | **테스트 없음** |
| 리뷰 워크플로우 | ✅ | ✅ | `GET/PATCH /api/v1/reviews` | ReviewServiceTest |
| 팔로업 태스크 | ✅ | ✅ | `GET/POST/PATCH /api/v1/tasks` | FollowUpTaskServiceTest |
| 카카오 웹훅 | ✅ | ✅ | `POST /api/webhook/kakao` | 3개 테스트 |
| 중복 감지/병합 | ✅ | ✅ | `POST /api/v1/duplicates` | DuplicatePolicyServiceTest |
| 알림/알림톡 | ✅ | ✅ | `GET/POST /api/v1/notifications` | AlimtalkServiceTest |
| 통계 | ✅ | ✅ | `GET /api/v1/stats` | StatsServiceTest |

### 2.2 프론트엔드 기능 매트릭스

| 기능 | 구현 | 백엔드 연동 | 비고 |
|------|:----:|:---------:|------|
| 대시보드 (통계 + 오늘 할 일) | ✅ | ✅ | Statistics + TodayTasks 컴포넌트 |
| 고객 목록 | ✅ | ✅ | ClientList 컴포넌트 |
| 고객 상세 | ✅ | ✅ | ClientDetail + ConsultationSection |
| 리뷰 박스 | ✅ | ✅ | ReviewBox 컴포넌트 |
| 하단 네비게이션 | ✅ | - | 3탭 모바일 UI |
| **메모리 검색 UI** | ❌ | - | API는 완성, **프론트 미구현** |
| **OCR 스크린샷 업로드 UI** | ❌ | - | API는 완성, **프론트 미구현** |

---

## 3. 아키텍처 원칙 준수 점검

| 원칙 | 상태 | 세부 |
|------|:----:|------|
| domain 순수 Kotlin (Spring 의존성 없음) | ✅ | 11개 도메인 파일 모두 순수 Kotlin |
| 엔티티 UUID ID, FK는 SQL에서 강제 | ✅ | `@ManyToOne` 없음, Flyway에서 FK 정의 |
| JPA 엔티티 class (data class X) | ✅ | equals/hashCode/copy 문제 방지 |
| owner_id 멀티테넌시 | ✅ | 모든 엔티티 + API에서 ownerId 필터링 |
| 프롬프트 `.st` 외부화 | ⚠️ 부분 | `relationship-summary.st`만 외부화, OCR은 코드 내 인라인 |
| DTO request/response 분리 | ✅ | toCommand() / toResponse() 패턴 일관 적용 |
| REST API `/api/v1/` prefix | ✅ | 모든 엔드포인트 준수 |
| Pageable 페이지네이션 | ✅ | 목록 조회 API에 Spring Data Pageable 사용 |

---

## 4. 코드 규모

| 영역 | 파일 수 | 비고 |
|------|------:|------|
| Domain (엔티티/상태) | 11 | 순수 Kotlin |
| Application (서비스/커맨드) | 16 | 유스케이스 오케스트레이션 |
| Infrastructure (AI/DB/외부) | 11 | Spring AI, JPA, Kakao |
| Web (컨트롤러/DTO) | 12 | REST API + Webhook |
| **백엔드 소스 합계** | **51** | `src/main/kotlin` |
| 테스트 | 13 | `src/test/kotlin` |
| Flyway 마이그레이션 | 6 | V1 ~ V6 |
| 프롬프트 템플릿 | 1 | `.st` 파일 |
| 프론트엔드 | 21 | Next.js 14 (pages, components, types, api) |
| 문서 | 10+ | CLAUDE.md, docs/, ai-spec/ |

---

## 5. 테스트 커버리지

### 테스트 있는 영역 (10개)

| 테스트 파일 | 대상 |
|------------|------|
| `CustomerServiceTest` | 고객 CRUD |
| `OcrExtractionServiceTest` | OCR 핵심 로직 |
| `OcrExtractionServiceReviewTaskTest` | OCR → 리뷰 태스크 워크플로우 |
| `OcrQualityNotificationTest` | OCR 품질 알림 |
| `ReviewServiceTest` | 리뷰 상태 머신 |
| `FollowUpTaskServiceTest` | 팔로업 태스크 관리 |
| `KakaoWebhookServiceTest` | 카카오 웹훅 처리 |
| `KakaoWebhookDuplicateTest` | 웹훅 중복 감지 |
| `KakaoWebhookQualityTest` | 웹훅 품질 검증 |
| `DuplicatePolicyServiceTest` | 중복 병합 정책 |
| `AlimtalkServiceTest` | 알림톡 스케줄링 |
| `StatsServiceTest` | 통계 집계 |
| `DefaultKakaoSignatureVerifierTest` | 카카오 서명 검증 |

### 테스트 없는 영역 (갭)

| 영역 | 우선순위 | 이유 |
|------|:--------:|------|
| `ConsultationService` (CRUD + 임베딩 훅) | **높음** | 핵심 비즈니스 로직, 임베딩 실패 시 안전 동작 검증 필요 |
| `ConsultationEmbeddingService` | **높음** | VectorStore 연동, 텍스트 조합/자르기 로직 검증 |
| `RelationshipMemoryService` | **높음** | 시맨틱 검색 + AI 요약, 빈 결과 핸들링 검증 |
| `MemoryController` | 보통 | API 레이어, 입력 검증 |
| `ConsultationController` | 보통 | API 레이어 |

---

## 6. 주요 갭 & 다음 단계 후보

### 6.1 기술 부채

| # | 항목 | 심각도 | 설명 |
|---|------|:------:|------|
| G1 | Step 6-7 테스트 부재 | **높음** | 임베딩/메모리 서비스 단위 테스트 미작성 |
| G2 | ConsultationService 테스트 부재 | **높음** | CRUD + 임베딩 훅 통합 테스트 미작성 |
| G3 | OCR 프롬프트 인라인 | 낮음 | `AnthropicOcrExtractor`에 하드코딩, `.st` 외부화 가능 |
| G4 | CLAUDE.md 로드맵 업데이트 필요 | 낮음 | 3~9단계 취소선 미적용 |

### 6.2 프론트엔드 갭

| # | 항목 | 설명 |
|---|------|------|
| F1 | 메모리 검색 UI | `/api/v1/memory/search`, `/summarize` 호출 화면 없음 |
| F2 | OCR 스크린샷 업로드 UI | `/api/v1/ocr/extract` 호출 화면 없음 |
| F3 | 미커밋 프론트 변경분 | `ConsultationSection.tsx` 등 4개 파일 미스테이징 |

### 6.3 운영 준비

| # | 항목 | 설명 |
|---|------|------|
| O1 | 인증/인가 | 현재 ownerId를 클라이언트가 직접 전송 → Spring Security 필요 |
| O2 | 배포 파이프라인 | CI/CD 미설정 |
| O3 | 모니터링 | 헬스체크, 메트릭, 에러 추적 미설정 |
| O4 | 알림톡 실연동 | 현재 MockAlimtalkSenderGateway → 실제 카카오 알림톡 API 연동 필요 |

---

## 7. API 전체 목록

```
# 고객
GET    /api/v1/customers              # 고객 목록 (페이지네이션, 검색)
POST   /api/v1/customers              # 고객 생성
GET    /api/v1/customers/{id}         # 고객 상세
PATCH  /api/v1/customers/{id}         # 고객 수정
DELETE /api/v1/customers/{id}         # 고객 삭제

# 상담
GET    /api/v1/consultations          # 상담 목록 (ownerId, customerId 필터)
POST   /api/v1/consultations          # 상담 생성 → 임베딩 자동 저장
GET    /api/v1/consultations/{id}     # 상담 상세
PATCH  /api/v1/consultations/{id}     # 상담 수정 → 임베딩 자동 갱신
DELETE /api/v1/consultations/{id}     # 상담 삭제 → 임베딩 자동 삭제

# OCR
POST   /api/v1/ocr/extract           # 스크린샷 OCR 추출

# 리뷰
GET    /api/v1/reviews                # 리뷰 태스크 목록
PATCH  /api/v1/reviews/{id}           # 리뷰 상태 변경

# 팔로업 태스크
GET    /api/v1/tasks                  # 팔로업 목록
POST   /api/v1/tasks                  # 팔로업 생성
PATCH  /api/v1/tasks/{id}             # 팔로업 상태 변경

# 메모리 (관계 기억)
POST   /api/v1/memory/search          # 시맨틱 유사도 검색
POST   /api/v1/memory/summarize       # AI 관계 요약 생성

# 중복 감지
POST   /api/v1/duplicates             # 중복 고객 감지/병합

# 알림
GET    /api/v1/notifications          # 알림 목록
POST   /api/v1/notifications          # 알림 발송

# 통계
GET    /api/v1/stats                  # 대시보드 통계

# 웹훅
POST   /api/webhook/kakao             # 카카오톡 웹훅 수신
```

---

## 8. 기술 스택 요약

| 영역 | 기술 | 버전 |
|------|------|------|
| 언어 | Kotlin | 2.1.0 |
| JVM | Java | 21 |
| 프레임워크 | Spring Boot | 3.4.1 |
| AI (채팅/비전) | Spring AI + Anthropic Claude | 1.0.0 |
| AI (임베딩) | Spring AI + OpenAI text-embedding-3-small | 1.0.0 |
| 벡터 저장소 | PgVectorStore (HNSW, COSINE, 1536d) | 1.0.0 |
| 데이터베이스 | PostgreSQL + pgvector | 16 |
| 마이그레이션 | Flyway | - |
| 빌드 | Gradle Kotlin DSL | 8.12 |
| 코드 스타일 | ktlint | 14.0.1 |
| 프론트엔드 | Next.js + TypeScript + Tailwind CSS | 14.2.3 |
| 컨테이너 | Docker Compose | - |
