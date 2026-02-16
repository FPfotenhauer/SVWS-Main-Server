CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE schule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    svws_url VARCHAR(512) NOT NULL,
    svws_username VARCHAR(255) NOT NULL,
    svws_password VARCHAR(512) NOT NULL,
    status VARCHAR(50) NOT NULL,
    last_sync_at TIMESTAMPTZ,
    last_sync_status VARCHAR(50),
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
