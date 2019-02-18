# Migration from 0.21.0 to 0.22.0

## Changes Since 0.21.0

### Database Changes

We added `NOT NULL` definitions to database tables to improve data integrity.

DDL update script for Oracle:
```
ALTER TABLE ns_auth_method MODIFY auth_method NOT NULL;
ALTER TABLE ns_auth_method MODIFY order_number NOT NULL;
ALTER TABLE ns_auth_method MODIFY check_auth_fails NOT NULL;
ALTER TABLE ns_user_prefs MODIFY user_id NOT NULL;
ALTER TABLE ns_operation_config MODIFY operation_name NOT NULL;
ALTER TABLE ns_operation_config MODIFY template_version NOT NULL;
ALTER TABLE ns_operation_config MODIFY template_id NOT NULL;
ALTER TABLE ns_operation_config MODIFY mobile_token_mode NOT NULL;
ALTER TABLE ns_operation MODIFY operation_id NOT NULL;
ALTER TABLE ns_operation MODIFY operation_name NOT NULL;
ALTER TABLE ns_operation MODIFY operation_data NOT NULL;
ALTER TABLE ns_operation_history MODIFY operation_id NOT NULL;
ALTER TABLE ns_operation_history MODIFY result_id NOT NULL;
ALTER TABLE ns_operation_history MODIFY request_auth_method NOT NULL;
ALTER TABLE ns_operation_history MODIFY request_auth_step_result NOT NULL;
ALTER TABLE ns_operation_history MODIFY response_result NOT NULL;
ALTER TABLE ns_step_definition MODIFY step_definition_id NOT NULL;
ALTER TABLE ns_step_definition MODIFY operation_name NOT NULL;
ALTER TABLE ns_step_definition MODIFY operation_type NOT NULL;
ALTER TABLE ns_step_definition MODIFY response_priority NOT NULL;
ALTER TABLE ns_step_definition MODIFY response_result NOT NULL;
ALTER TABLE wf_operation_session MODIFY operation_id NOT NULL;
ALTER TABLE wf_operation_session MODIFY http_session_id NOT NULL;
ALTER TABLE wf_operation_session MODIFY result NOT NULL;
ALTER TABLE da_sms_authorization MODIFY message_id NOT NULL;
ALTER TABLE da_sms_authorization MODIFY operation_id NOT NULL;
ALTER TABLE da_sms_authorization MODIFY user_id NOT NULL;
ALTER TABLE da_sms_authorization MODIFY operation_name NOT NULL;
ALTER TABLE da_sms_authorization MODIFY authorization_code NOT NULL;
ALTER TABLE da_sms_authorization MODIFY salt NOT NULL;
ALTER TABLE da_sms_authorization MODIFY message_text NOT NULL;
```

DDL update script for MySQL:
```
ALTER TABLE ns_auth_method MODIFY auth_method VARCHAR(32) PRIMARY KEY NOT NULL;
ALTER TABLE ns_auth_method MODIFY order_number INTEGER NOT NULL;
ALTER TABLE ns_auth_method MODIFY check_auth_fails BOOLEAN NOT NULL;
ALTER TABLE ns_user_prefs MODIFY user_id VARCHAR(256) NOT NULL;
ALTER TABLE ns_operation_config MODIFY operation_name VARCHAR(32) NOT NULL;
ALTER TABLE ns_operation_config MODIFY template_version CHAR NOT NULL;
ALTER TABLE ns_operation_config MODIFY template_id INTEGER NOT NULL;
ALTER TABLE ns_operation_config MODIFY mobile_token_mode VARCHAR(256) NOT NULL;
ALTER TABLE ns_operation MODIFY operation_id VARCHAR(256) NOT NULL;
ALTER TABLE ns_operation MODIFY operation_name VARCHAR(32) NOT NULL;
ALTER TABLE ns_operation MODIFY operation_data TEXT NOT NULL;
ALTER TABLE ns_operation_history MODIFY operation_id VARCHAR(256) NOT NULL;
ALTER TABLE ns_operation_history MODIFY result_id INTEGER NOT NULL;
ALTER TABLE ns_operation_history MODIFY request_auth_method VARCHAR(32) NOT NULL;
ALTER TABLE ns_operation_history MODIFY request_auth_step_result VARCHAR(32) NOT NULL;
ALTER TABLE ns_operation_history MODIFY response_result VARCHAR(32) NOT NULL;
ALTER TABLE ns_step_definition MODIFY step_definition_id INTEGER NOT NULL;
ALTER TABLE ns_step_definition MODIFY operation_name VARCHAR(32) NOT NULL;
ALTER TABLE ns_step_definition MODIFY operation_type VARCHAR(32) NOT NULL;
ALTER TABLE ns_step_definition MODIFY response_priority INTEGER NOT NULL;
ALTER TABLE ns_step_definition MODIFY response_result VARCHAR(32) NOT NULL;
ALTER TABLE wf_operation_session MODIFY operation_id VARCHAR(256) NOT NULL;
ALTER TABLE wf_operation_session MODIFY http_session_id VARCHAR(256) NOT NULL;
ALTER TABLE wf_operation_session MODIFY result VARCHAR(32) NOT NULL;
ALTER TABLE da_sms_authorization MODIFY message_id VARCHAR(256) NOT NULL;
ALTER TABLE da_sms_authorization MODIFY operation_id VARCHAR(256) NOT NULL;
ALTER TABLE da_sms_authorization MODIFY user_id VARCHAR(256) NOT NULL;
ALTER TABLE da_sms_authorization MODIFY operation_name VARCHAR(32) NOT NULL;
ALTER TABLE da_sms_authorization MODIFY authorization_code VARCHAR(32) NOT NULL;
ALTER TABLE da_sms_authorization MODIFY salt VARBINARY(16) NOT NULL;
ALTER TABLE da_sms_authorization MODIFY message_text TEXT NOT NULL;
```
