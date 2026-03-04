CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create SVWS servers table
CREATE TABLE svws_servers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    base_url VARCHAR(512) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password_encrypted VARCHAR(512) NOT NULL,
    status VARCHAR(50) DEFAULT 'UNTESTED',
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_svws_servers_name ON svws_servers(name);

CREATE TABLE schule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    svws_server_id UUID REFERENCES svws_servers(id) ON DELETE CASCADE,
    svws_schema VARCHAR(255) NOT NULL,
    svws_username VARCHAR(255),
    svws_user_password_encrypted VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
