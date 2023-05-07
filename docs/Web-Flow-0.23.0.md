# Migration from 0.22.0 to 0.23.0

## Changes Since 0.22.0

### Database Changes

Following database changes were introduced in version `0.23.0`:
 
- Added `afs_enabled` and `afs_config_id` columns to table `ns_operation_config`
- Added `operation_hash`, `websocket_session_id` and `client_ip_address` columns to table `wf_operation_session`
- Added `request_auth_instruments` column to table `ns_operation_history`
- Added `user_account_status` and `external_transaction_id` columns to table `ns_operation`
- New tables `ns_operation_afs` and `wf_afs_config` for integration of anti-fraud system
- Updated indexes and sequences
  
DDL update script for Oracle:
```sql
CREATE SEQUENCE ns_operation_afs_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;

ALTER TABLE ns_operation_config ADD afs_enabled NUMBER(1) DEFAULT 0 NOT NULL;
ALTER TABLE ns_operation_config ADD afs_config_id VARCHAR(256);

ALTER TABLE wf_operation_session ADD operation_hash VARCHAR(256);
ALTER TABLE wf_operation_session ADD websocket_session_id VARCHAR(32);
ALTER TABLE wf_operation_session ADD client_ip_address VARCHAR(32);

ALTER TABLE ns_operation ADD application_original_scopes VARCHAR(256);
ALTER TABLE ns_operation ADD external_transaction_id VARCHAR(256);

CREATE INDEX wf_operation_hash ON wf_operation_session (operation_hash);
CREATE INDEX wf_websocket_session ON wf_operation_session (websocket_session_id);

ALTER TABLE ns_operation_history ADD request_auth_instruments VARCHAR(256);
ALTER TABLE ns_operation_history ADD mobile_token_active NUMBER(1) DEFAULT 0 NOT NULL;
ALTER TABLE ns_operation ADD user_account_status VARCHAR(32);

CREATE TABLE ns_operation_afs (
  afs_action_id               INTEGER PRIMARY KEY NOT NULL,
  operation_id                VARCHAR(256) NOT NULL,
  request_afs_action          VARCHAR(256) NOT NULL,
  request_step_index          INTEGER NOT NULL,
  request_afs_extras          VARCHAR(256),
  response_afs_apply          NUMBER(1) DEFAULT 0 NOT NULL,
  response_afs_label          VARCHAR(256),
  response_afs_extras         VARCHAR(256),
  timestamp_created           TIMESTAMP,
  CONSTRAINT operation_afs_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id)
);

CREATE TABLE wf_afs_config (
  config_id                 VARCHAR(256) PRIMARY KEY NOT NULL,
  js_snippet_url            VARCHAR(256) NOT NULL,
  parameters                CLOB
);

CREATE UNIQUE INDEX ns_operation_afs_unique on ns_operation_afs (operation_id, request_afs_action, request_step_index);
```

DDL update script for MySQL:
```sql
ALTER TABLE `ns_operation_config` ADD `afs_enabled` BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE `ns_operation_config` ADD `afs_config_id` VARCHAR(256);

ALTER TABLE `wf_operation_session` ADD `operation_hash` VARCHAR(256);  
ALTER TABLE `wf_operation_session` ADD `websocket_session_id` VARCHAR(32);
ALTER TABLE `wf_operation_session` ADD `client_ip_address` VARCHAR(32);

ALTER TABLE `ns_operation` ADD `application_original_scopes` VARCHAR(256);
ALTER TABLE `ns_operation` ADD `external_transaction_id` VARCHAR(256);

CREATE INDEX `wf_operation_hash` ON `wf_operation_session` (`operation_hash`);
CREATE INDEX `wf_websocket_session` ON `wf_operation_session` (`websocket_session_id`);

ALTER TABLE `ns_operation_history` ADD `request_auth_instruments` VARCHAR(256);
ALTER TABLE `ns_operation_history` ADD `mobile_token_active` BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE `ns_operation` ADD `user_account_status` VARCHAR(32);

CREATE TABLE ns_operation_afs (
  afs_action_id               INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
  operation_id                VARCHAR(256) NOT NULL,
  request_afs_action          VARCHAR(256) NOT NULL,
  request_step_index          INTEGER NOT NULL,
  request_afs_extras          VARCHAR(256),
  response_afs_apply          BOOLEAN NOT NULL DEFAULT FALSE,
  response_afs_label          VARCHAR(256),
  response_afs_extras         VARCHAR(256),
  timestamp_created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY operation_afs_fk (operation_id) REFERENCES ns_operation (operation_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


CREATE TABLE wf_afs_config (
  config_id                 VARCHAR(256) PRIMARY KEY NOT NULL,
  js_snippet_url            VARCHAR(256) NOT NULL,
  parameters                TEXT
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE UNIQUE INDEX `ns_operation_afs_unique` on `ns_operation_afs` (`operation_id`, `request_afs_action`, `request_step_index`);
```

### Configuration of OAuth 2.1 Client

Due to changes in Spring OAuth 2.1 configuration, the redirect URI needs to be specified in the client configuration in database.
For demo application, you can use this SQL query:

```sql
UPDATE oauth_client_details SET web_server_redirect_uri = 'http://localhost:8080/powerauth-webflow-client/connect/demo' WHERE client_id='democlient';
COMMIT;
```

Note that the URI needs to be updated for each client in each environment. There is typically a different redirect URI 
for development, testing and production environments.

### Other Changes

#### Storing Original OAuth 2.1 Scopes

When assigning the application context to the operation, it is now possible to pass OAuth 2.1 scopes as a strongly typed attribute via the `originalScopes` attribute, like so:

```json
{
  "requestObject": {
    "operationId": "12341234-1234-1234-1234-123412341234",
    "applicationContext": {
      "id" : "democlient",
      "name" : "Demo application",
      "description" : "Demo application",
      "originalScopes" : [ "aisp", "pisp" ],
      "extras" : {
        "applicationOwner" : "Wultra"
      }
    }
  }
}
```

This is helpful when working with the operation later, especially when fetching the correct consent for the operation.
