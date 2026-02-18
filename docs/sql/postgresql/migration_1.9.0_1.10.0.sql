-- Create a new table shedlock
CREATE TABLE IF NOT EXISTS shedlock (name VARCHAR(64) NOT NULL, lock_until TIMESTAMP WITHOUT TIME ZONE NOT NULL, locked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL, locked_by VARCHAR(255) NOT NULL, CONSTRAINT shedlock_pkey PRIMARY KEY (name));

-- Changeset powerauth-nextstep/1.10.x/20250715-add-location-setting::1::Zdenek Cerny
-- Add column source to ns_credential_storage
ALTER TABLE ns_credential_storage ADD source VARCHAR(32) DEFAULT 'LOCAL';

-- Changeset powerauth-nextstep/1.10.x/20250715-add-location-setting::2::Zdenek Cerny
-- Add column target to ns_credential_storage
ALTER TABLE ns_credential_storage ADD target VARCHAR(32) DEFAULT 'LOCAL';

ALTER TABLE ns_credential_storage ADD external_reference VARCHAR(256);

