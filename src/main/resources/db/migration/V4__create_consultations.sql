-- 상담 기록 테이블
CREATE TABLE consultations (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id          UUID            NOT NULL,
    customer_id       UUID            NOT NULL,
    ingest_job_id     UUID,
    summary           TEXT            NOT NULL,
    raw_text          TEXT,
    consultation_date TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_consultations_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_consultations_ingest_job
        FOREIGN KEY (ingest_job_id) REFERENCES ingest_jobs (id)
);

-- 고객별 상담 이력 조회
CREATE INDEX idx_consultations_customer ON consultations (customer_id);

-- 소유자 + 고객별 조회
CREATE INDEX idx_consultations_owner_customer ON consultations (owner_id, customer_id);

-- IngestJob으로 상담 조회
CREATE INDEX idx_consultations_ingest_job ON consultations (ingest_job_id)
    WHERE ingest_job_id IS NOT NULL;
