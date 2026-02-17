-- 팔로업 작업 테이블
CREATE TABLE follow_up_tasks (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id         UUID            NOT NULL,
    customer_id      UUID            NOT NULL,
    consultation_id  UUID,
    objective        VARCHAR(500)    NOT NULL,
    draft_content    TEXT,
    status           VARCHAR(20)     NOT NULL DEFAULT 'SCHEDULED',
    scheduled_at     TIMESTAMPTZ,
    sent_at          TIMESTAMPTZ,
    snoozed_until    TIMESTAMPTZ,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_follow_up_tasks_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_follow_up_tasks_consultation
        FOREIGN KEY (consultation_id) REFERENCES consultations (id)
);

-- 소유자 + 상태별 조회
CREATE INDEX idx_follow_up_tasks_owner_status ON follow_up_tasks (owner_id, status);

-- 고객별 팔로업 조회
CREATE INDEX idx_follow_up_tasks_customer ON follow_up_tasks (customer_id);

-- 스케줄링 대상 조회 (SCHEDULED 상태만)
CREATE INDEX idx_follow_up_tasks_scheduled
    ON follow_up_tasks (status, scheduled_at)
    WHERE status = 'SCHEDULED';
