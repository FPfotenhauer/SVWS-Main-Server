-- Create SVWS servers table
CREATE TABLE svws_servers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    base_url VARCHAR(512) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password_encrypted VARCHAR(512) NOT NULL,
    status VARCHAR(50) DEFAULT 'UNTESTED',
    last_error TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_svws_servers_name ON svws_servers(name);
