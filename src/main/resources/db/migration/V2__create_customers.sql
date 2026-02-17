-- 고객 테이블
CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id        UUID            NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20),
    email           VARCHAR(255),
    kakao_user_key  VARCHAR(100),
    project_type    VARCHAR(100),
    estimated_budget VARCHAR(50),
    inquiry_summary TEXT,
    notes           TEXT,
    source          VARCHAR(20)     NOT NULL DEFAULT 'MANUAL',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- 소유자별 조회 인덱스
CREATE INDEX idx_customers_owner_id ON customers (owner_id);

-- 카카오 사용자 키로 고객 조회 (NULL 제외 부분 인덱스)
CREATE UNIQUE INDEX idx_customers_owner_kakao
    ON customers (owner_id, kakao_user_key)
    WHERE kakao_user_key IS NOT NULL;

-- 이름 검색 인덱스
CREATE INDEX idx_customers_owner_name ON customers (owner_id, name);
