-- 수집 작업 테이블 (카카오 웹훅 → OCR → 구조화 파이프라인)
CREATE TABLE ingest_jobs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id            UUID            NOT NULL,
    event_id            VARCHAR(100),
    message_id          VARCHAR(100),
    channel_id          VARCHAR(100),
    kakao_user_key      VARCHAR(100),
    source_type         VARCHAR(20)     NOT NULL,
    source_url          VARCHAR(2048),
    idempotency_key     VARCHAR(255)    NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'RECEIVED',
    overall_confidence  DOUBLE PRECISION,
    extraction_result   JSONB,
    ocr_raw_text        TEXT,
    error_reason        TEXT,
    received_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    processed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_ingest_jobs_idempotency_key UNIQUE (idempotency_key)
);

-- 멱등성 키 조회 (UNIQUE 제약에 의한 인덱스 자동 생성)
-- 소유자 + 상태별 조회
CREATE INDEX idx_ingest_jobs_owner_status ON ingest_jobs (owner_id, status);

-- 신뢰도 임계치 쿼리용
CREATE INDEX idx_ingest_jobs_confidence ON ingest_jobs (overall_confidence)
    WHERE overall_confidence IS NOT NULL;
