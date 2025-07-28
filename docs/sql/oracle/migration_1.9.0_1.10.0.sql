-- Create a new table shedlock
CREATE TABLE shedlock (name VARCHAR2(64) NOT NULL, lock_until TIMESTAMP NOT NULL, locked_at TIMESTAMP NOT NULL, locked_by VARCHAR2(255) NOT NULL, CONSTRAINT PK_SHEDLOCK PRIMARY KEY (name));

-- Changeset powerauth-nextstep/1.10.x/20250715-add-location-setting::1::Zdenek Cerny
-- Add column source to ns_credential_storage
ALTER TABLE ns_credential_storage ADD source VARCHAR2(32) DEFAULT 'LOCAL';

-- Changeset powerauth-nextstep/1.10.x/20250715-add-location-setting::2::Zdenek Cerny
-- Add column target to ns_credential_storage
ALTER TABLE ns_credential_storage ADD target VARCHAR2(32) DEFAULT 'LOCAL';
