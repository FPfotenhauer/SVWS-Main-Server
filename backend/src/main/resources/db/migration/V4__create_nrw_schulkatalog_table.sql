-- Create NRW school catalog table
CREATE TABLE nrw_schulkatalog (
    id UUID PRIMARY KEY,
    schulnummer VARCHAR(50) NOT NULL UNIQUE,
    schulname VARCHAR(255) NOT NULL,
    schultyp VARCHAR(100),
    strasse VARCHAR(255),
    plz VARCHAR(10),
    ort VARCHAR(255),
    kreis VARCHAR(100),
    schulamt VARCHAR(255),
    telefon VARCHAR(255),
    fax VARCHAR(255),
    email VARCHAR(255),
    homepage VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for search performance
CREATE INDEX idx_nrw_schulkatalog_schulname ON nrw_schulkatalog(schulname);
CREATE INDEX idx_nrw_schulkatalog_ort ON nrw_schulkatalog(ort);
CREATE INDEX idx_nrw_schulkatalog_kreis ON nrw_schulkatalog(kreis);
CREATE INDEX idx_nrw_schulkatalog_schulnummer ON nrw_schulkatalog(schulnummer);

-- Create table for sync metadata
CREATE TABLE nrw_schulkatalog_sync (
    id UUID PRIMARY KEY,
    last_sync_at TIMESTAMP,
    last_error TEXT,
    total_count BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
