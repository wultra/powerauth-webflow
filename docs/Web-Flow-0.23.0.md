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

CREATE INDEX `wf_operation_hash` ON `wf_operation_session` (`operation_hash`);
CREATE INDEX `wf_websocket_session` ON `wf_operation_session` (`websocket_session_id`);
```

### Configuration of OAuth 2.0 Client

Due to changes in Spring OAuth 2.0 configuration, the redirect URI needs to be specified in the client configuration in database.
For demo application, you can use this SQL query:

```sql
UPDATE oauth_client_details SET web_server_redirect_uri = 'http://localhost:8080/powerauth-webflow-client/connect/demo' WHERE client_id='democlient';
COMMIT;
```

Note that the URI needs to be updated for each client in each environment. There is typically a different redirect URI 
for development, testing and production environments.
