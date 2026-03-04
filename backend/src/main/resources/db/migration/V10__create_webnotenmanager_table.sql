-- Create webnotenmanager table for managing Webnotenmanger servers per school
CREATE TABLE webnotenmanager (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schule_id UUID NOT NULL REFERENCES schule(id) ON DELETE CASCADE,
    notenserver_base_url VARCHAR(512) NOT NULL,
    oauth_secret_encrypted VARCHAR(512) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create index for efficient lookups by school
CREATE INDEX idx_webnotenmanager_schule_id ON webnotenmanager(schule_id);

-- Create unique constraint to ensure only one webnotenmanager config per school
CREATE UNIQUE INDEX idx_webnotenmanager_schule_unique ON webnotenmanager(schule_id);
