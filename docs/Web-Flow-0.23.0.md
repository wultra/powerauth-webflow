# Migration from 0.22.0 to 0.23.0

## Changes Since 0.22.0

### Database Changes

Following database changes were introduced in version `0.23.0`:
 
- We added `afs_enabled` column to table `ns_operation_config`
- We added `operation_hash` and `websocket_session_id` columns to table `wf_operation_session`
  
DDL update script for Oracle:
```sql
ALTER TABLE ns_operation_config ADD afs_enabled NUMBER(1) DEFAULT 0 NOT NULL;

ALTER TABLE wf_operation_session ADD operation_hash VARCHAR(256) DEFAULT 0 NOT NULL;
ALTER TABLE wf_operation_session ADD websocket_session_id VARCHAR(32) DEFAULT 0 NOT NULL;

CREATE INDEX wf_operation_hash ON wf_operation_session (operation_hash);
CREATE INDEX wf_websocket_session ON wf_operation_session (websocket_session_id);
```

DDL update script for MySQL:
```sql
ALTER TABLE `ns_operation_config` ADD `afs_enabled` BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE `wf_operation_session` ADD `operation_hash` VARCHAR(256),  
ALTER TABLE `wf_operation_session` ADD `websocket_session_id` VARCHAR(32),

CREATE INDEX wf_operation_hash ON wf_operation_session (operation_hash);
CREATE INDEX wf_websocket_session ON wf_operation_session (websocket_session_id);
```

