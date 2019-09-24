# Migration from 0.22.0 to 0.23.0

## Changes Since 0.22.0

### Database Changes

Following database changes were introduced in version `0.23.0`:
 
- Added `afs_enabled` and `afs_config_id` columns to table `ns_operation_config`
- Added `operation_hash` and `websocket_session_id` columns to table `wf_operation_session`
- New table `wf_afs_config` for configuration of anti-fraud system
  
DDL update script for Oracle:
```sql
ALTER TABLE ns_operation_config ADD afs_enabled NUMBER(1) DEFAULT 0 NOT NULL;
ALTER TABLE ns_operation_config ADD afs_config_id VARCHAR(256);

ALTER TABLE wf_operation_session ADD operation_hash VARCHAR(256);
ALTER TABLE wf_operation_session ADD websocket_session_id VARCHAR(32);
ALTER TABLE wf_operation_session ADD client_ip VARCHAR(32);

CREATE INDEX wf_operation_hash ON wf_operation_session (operation_hash);
CREATE INDEX wf_websocket_session ON wf_operation_session (websocket_session_id);

CREATE TABLE wf_afs_config (
  config_id                 VARCHAR(256) PRIMARY KEY NOT NULL,
  js_snippet_url            VARCHAR(256) NOT NULL,
  parameters                CLOB
);
```

DDL update script for MySQL:
```sql
ALTER TABLE `ns_operation_config` ADD `afs_enabled` BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE `ns_operation_config` ADD `afs_config_id` VARCHAR(256);

ALTER TABLE `wf_operation_session` ADD `operation_hash` VARCHAR(256),  
ALTER TABLE `wf_operation_session` ADD `websocket_session_id` VARCHAR(32),
ALTER TABLE `wf_operation_session` ADD `client_ip` VARCHAR(32),

CREATE INDEX wf_operation_hash ON wf_operation_session (operation_hash);
CREATE INDEX wf_websocket_session ON wf_operation_session (websocket_session_id);

CREATE TABLE wf_afs_config (
  config_id                 VARCHAR(256) PRIMARY KEY NOT NULL,
  js_snippet_url            VARCHAR(256) NOT NULL,
  parameters                TEXT
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
