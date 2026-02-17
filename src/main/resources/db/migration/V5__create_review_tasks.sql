-- 검토 작업 테이블 (신뢰도 낮은 추출 결과의 사용자 검토)
CREATE TABLE review_tasks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id            UUID            NOT NULL,
    ingest_job_id       UUID            NOT NULL,
    customer_guess      VARCHAR(100),
    uncertain_fields    JSONB,
    proposed_payload    JSONB,
    overall_confidence  DOUBLE PRECISION,
    status              VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
    expires_at          TIMESTAMPTZ,
    resolved_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_review_tasks_ingest_job
        FOREIGN KEY (ingest_job_id) REFERENCES ingest_jobs (id),
    CONSTRAINT uq_review_tasks_ingest_job UNIQUE (ingest_job_id)
);

-- 소유자 + 상태별 조회
CREATE INDEX idx_review_tasks_owner_status ON review_tasks (owner_id, status);

-- 만료 대상 조회 (OPEN/IN_PROGRESS 상태만)
CREATE INDEX idx_review_tasks_expires
    ON review_tasks (status, expires_at)
    WHERE status IN ('OPEN', 'IN_PROGRESS');
