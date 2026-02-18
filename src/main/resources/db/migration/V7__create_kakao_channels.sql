CREATE TABLE kakao_channels (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    UUID            NOT NULL,
    channel_id  VARCHAR(100)    NOT NULL,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_kakao_channels_channel_id UNIQUE (channel_id)
);

CREATE INDEX idx_kakao_channels_owner_id ON kakao_channels (owner_id);
