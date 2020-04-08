# Migration from 0.23.0 to 0.24.0

## Database Changes

Following database changes were introduced in version `0.24.0`:
 
- Added `client_certificate_used`, `client_certificate_cn`, `client_certificate_sn` columns to table `ns_operation`
  
DDL update script for Oracle:
```sql
-- Table wf_certificate_verification is used for storing information about verified client TLS certificates.
CREATE TABLE wf_certificate_verification (
  operation_id               VARCHAR(256) NOT NULL,
  auth_method                VARCHAR(32) NOT NULL,
  client_certificate_issuer  VARCHAR(4000) NOT NULL,
  client_certificate_subject VARCHAR(4000) NOT NULL,
  client_certificate_sn      VARCHAR(256) NOT NULL,
  operation_data             CLOB NOT NULL,
  timestamp_verified         TIMESTAMP NOT NULL,
  CONSTRAINT certificate_verification_pk PRIMARY KEY (operation_id, auth_method)
);
```

DDL update script for MySQL:
```sql
-- Table wf_certificate_verification is used for storing information about verified client TLS certificates.
CREATE TABLE wf_certificate_verification (
  operation_id               VARCHAR(256) NOT NULL,
  auth_method                VARCHAR(32) NOT NULL,
  client_certificate_issuer  VARCHAR(4000) NOT NULL,
  client_certificate_subject VARCHAR(4000) NOT NULL,
  client_certificate_sn      VARCHAR(256) NOT NULL,
  operation_data             TEXT NOT NULL,
  timestamp_verified         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (operation_id, auth_method)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

DDL update script for PostgreSQL:
```sql
-- Table wf_certificate_verification is used for storing information about verified client TLS certificates.
CREATE TABLE wf_certificate_verification (
  operation_id               VARCHAR(256) NOT NULL,
  auth_method                VARCHAR(32) NOT NULL,
  client_certificate_issuer  VARCHAR(4000) NOT NULL,
  client_certificate_subject VARCHAR(4000) NOT NULL,
  client_certificate_sn      VARCHAR(256) NOT NULL,
  operation_data             TEXT NOT NULL,
  timestamp_created          TIMESTAMP NOT NULL,
  CONSTRAINT certificate_verification_pk PRIMARY KEY (operation_id, auth_method)
);
```
