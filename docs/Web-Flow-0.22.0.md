# Migration from 0.21.0 to 0.22.0

## Changes Since 0.21.0

### Database Changes

Following database changes were introduced in version `0.22.0`:
 
- We added `NOT NULL` definitions to database tables to improve data integrity.
- New optional columns added in operations in table `ns_operation` for application context:
  - `application_id` - identifier of application
  - `application_name` - application name
  - `application_description` - application description
  - `application_extras` - map of extra values used in application context
- Table `ns_organization` has been added for organization context in Web Flow.
- Table `ns_operation` has new column `organization_id` for organization context.
- Table `da_sms_authorization` has new column `organization_id` for organization context.
  
DDL update script for Oracle:
```

-- Added not null constraints 

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

-- Columns for application context in table NS_OPERATION 

ALTER TABLE ns_operation ADD application_id VARCHAR(256);
ALTER TABLE ns_operation ADD application_name VARCHAR(256);
ALTER TABLE ns_operation ADD application_description VARCHAR(256);
ALTER TABLE ns_operation ADD application_extras CLOB;

-- New table ns_organization for organization context and update of tables ns_operation and da_sms_authorization

CREATE TABLE NS_OPERATION (
  organization_id          VARCHAR(256) PRIMARY KEY NOT NULL,
  display_name_key         VARCHAR(256),
  is_default               NUMBER(1) DEFAULT 0 NOT NULL,
  order_number             INTEGER NOT NULL
);

ALTER TABLE ns_operation ADD organization_id VARCHAR(256);

ALTER TABLE ns_operation ADD CONSTRAINT organization_fk FOREIGN KEY (organization_id) REFERENCES ns_organization (organization_id);

INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number) VALUES ('DEFAULT', null, 1, 1);

ALTER TABLE da_sms_authorization ADD organization_id VARCHAR(256);

ALTER TABLE ns_auth_method ADD has_mobile_token NUMBER(1) DEFAULT 0;

UPDATE ns_auth_method SET has_mobile_token = 1 WHERE auth_method = 'POWERAUTH_TOKEN';

COMMIT;
```

As the next step, please add new authentication methods `CONSENT`, `LOGIN_SCA` and `APPROVAL_SCA`. The `order_number` parameter should be updated to exceed maximum `order_number` in table `ns_auth_method` by 1 for `CONSENT`, by 2 for `LOGIN_SCA` and by 3 for `APPROVAL_SCA`.

```sql
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('CONSENT', 7, 0, NULL, NULL, 1, 5, 1, 0, 'method.consent');

INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('LOGIN_SCA', 8, FALSE, NULL, NULL, 1, 5, 1, 1, 'method.loginSca');

INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('APPROVAL_SCA', 9, FALSE, NULL, NULL, 1, 5, 1, 1, 'method.approvalSca');
```

DDL update script for MySQL:
```sql

-- Added not null constraints 

ALTER TABLE `ns_auth_method` MODIFY `auth_method` VARCHAR(32) NOT NULL;
ALTER TABLE `ns_auth_method` MODIFY `order_number` INTEGER NOT NULL;
ALTER TABLE `ns_auth_method` MODIFY `check_auth_fails` BOOLEAN NOT NULL;
ALTER TABLE `ns_user_prefs` MODIFY `user_id` VARCHAR(256) NOT NULL;
ALTER TABLE `ns_operation_config` MODIFY `operation_name` VARCHAR(32) NOT NULL;
ALTER TABLE `ns_operation_config` MODIFY `template_version` CHAR NOT NULL;
ALTER TABLE `ns_operation_config` MODIFY `template_id` INTEGER NOT NULL;
ALTER TABLE `ns_operation_config` MODIFY `mobile_token_mode` VARCHAR(256) NOT NULL;
ALTER TABLE `ns_operation` MODIFY `operation_id` VARCHAR(256) NOT NULL;
ALTER TABLE `ns_operation` MODIFY `operation_name` VARCHAR(32) NOT NULL;
ALTER TABLE `ns_operation` MODIFY `operation_data` TEXT NOT NULL;
ALTER TABLE `ns_operation_history` MODIFY `operation_id` VARCHAR(256) NOT NULL;
ALTER TABLE `ns_operation_history` MODIFY `result_id` INTEGER NOT NULL;
ALTER TABLE `ns_operation_history` MODIFY `request_auth_method` VARCHAR(32) NOT NULL;
ALTER TABLE `ns_operation_history` MODIFY `request_auth_step_result` VARCHAR(32) NOT NULL;
ALTER TABLE `ns_operation_history` MODIFY `response_result` VARCHAR(32) NOT NULL;
ALTER TABLE `ns_step_definition` MODIFY `step_definition_id` INTEGER NOT NULL;
ALTER TABLE `ns_step_definition` MODIFY `operation_name` VARCHAR(32) NOT NULL;
ALTER TABLE `ns_step_definition` MODIFY `operation_type` VARCHAR(32) NOT NULL;
ALTER TABLE `ns_step_definition` MODIFY `response_priority` INTEGER NOT NULL;
ALTER TABLE `ns_step_definition` MODIFY `response_result` VARCHAR(32) NOT NULL;
ALTER TABLE `wf_operation_session` MODIFY `operation_id` VARCHAR(256) NOT NULL;
ALTER TABLE `wf_operation_session` MODIFY `http_session_id` VARCHAR(256) NOT NULL;
ALTER TABLE `wf_operation_session` MODIFY `result` VARCHAR(32) NOT NULL;
ALTER TABLE `da_sms_authorization` MODIFY `message_id` VARCHAR(256) NOT NULL;
ALTER TABLE `da_sms_authorization` MODIFY `operation_id` VARCHAR(256) NOT NULL;
ALTER TABLE `da_sms_authorization` MODIFY `user_id` VARCHAR(256) NOT NULL;
ALTER TABLE `da_sms_authorization` MODIFY `operation_name` VARCHAR(32) NOT NULL;
ALTER TABLE `da_sms_authorization` MODIFY `authorization_code` VARCHAR(32) NOT NULL;
ALTER TABLE `da_sms_authorization` MODIFY `salt` VARBINARY(16) NOT NULL;
ALTER TABLE `da_sms_authorization` MODIFY `message_text` TEXT NOT NULL;

-- Columns for application context in table NS_OPERATION

ALTER TABLE `ns_operation` ADD `application_id` VARCHAR(256);
ALTER TABLE `ns_operation` ADD `application_name` VARCHAR(256);
ALTER TABLE `ns_operation` ADD `application_description` VARCHAR(256);
ALTER TABLE `ns_operation` ADD `application_extras` TEXT;

-- New table ns_organization for organization context and update of tables ns_operation and da_sms_authorization

CREATE TABLE `ns_organization` (
  organization_id          VARCHAR(256) PRIMARY KEY NOT NULL,
  display_name_key         VARCHAR(256),
  is_default               BOOLEAN NOT NULL,
  order_number             INTEGER NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `ns_operation` ADD `organization_id` VARCHAR(256);

ALTER TABLE `ns_operation` ADD FOREIGN KEY `organization_fk` (`organization_id`) REFERENCES `ns_organization` (`organization_id`);

INSERT INTO `ns_organization` (organization_id, display_name_key, is_default, order_number) VALUES ('DEFAULT', null, TRUE, 1);

ALTER TABLE `da_sms_authorization` ADD `organization_id` VARCHAR(256);

ALTER TABLE `ns_auth_method` ALTER COLUMN `user_prefs_default` SET DEFAULT FALSE;
ALTER TABLE `ns_auth_method` ALTER COLUMN `has_user_interface` SET DEFAULT FALSE;
ALTER TABLE `ns_auth_method` ALTER COLUMN `has_mobile_token` SET DEFAULT FALSE;
ALTER TABLE `ns_user_prefs` ALTER COLUMN `auth_method_1` SET DEFAULT FALSE;
ALTER TABLE `ns_user_prefs` ALTER COLUMN `auth_method_2` SET DEFAULT FALSE;
ALTER TABLE `ns_user_prefs` ALTER COLUMN `auth_method_3` SET DEFAULT FALSE;
ALTER TABLE `ns_user_prefs` ALTER COLUMN `auth_method_4` SET DEFAULT FALSE;
ALTER TABLE `ns_user_prefs` ALTER COLUMN `auth_method_5` SET DEFAULT FALSE;
ALTER TABLE `da_sms_authorization` ALTER COLUMN `verified` SET DEFAULT FALSE;


UPDATE `ns_auth_method` SET `user_prefs_default` = FALSE WHERE `user_prefs_default` IS NULL;
UPDATE `ns_auth_method` SET `has_user_interface` = FALSE WHERE `has_user_interface` IS NULL;
UPDATE `ns_auth_method` SET `has_mobile_token` = FALSE WHERE `has_mobile_token` IS NULL;
UPDATE `ns_user_prefs` SET `auth_method_1` = FALSE WHERE `auth_method_1` IS NULL;
UPDATE `ns_user_prefs` SET `auth_method_2` = FALSE WHERE `auth_method_2` IS NULL;
UPDATE `ns_user_prefs` SET `auth_method_3` = FALSE WHERE `auth_method_3` IS NULL;
UPDATE `ns_user_prefs` SET `auth_method_4` = FALSE WHERE `auth_method_4` IS NULL;
UPDATE `ns_user_prefs` SET `auth_method_5` = FALSE WHERE `auth_method_5` IS NULL;
UPDATE `da_sms_authorization` SET `verified` = FALSE WHERE `verified` IS NULL;

ALTER TABLE `da_sms_authorization` ADD `has_mobile_token` BOOLEAN DEFAULT FALSE;

UPDATE `ns_auth_method` SET `has_mobile_token` = TRUE WHERE `auth_method` = 'POWERAUTH_TOKEN';
```

As the next step, please add new authentication methods `CONSENT`, `LOGIN_SCA` and `APPROVAL_SCA`. The `order_number` parameter should be updated to exceed maximum `order_number` in table `ns_auth_method` by 1 for `CONSENT`, by 2 for `LOGIN_SCA` and by 3 for `APPROVAL_SCA`.

```sql
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('CONSENT', 7, FALSE, NULL, NULL, TRUE, 5, TRUE, FALSE, 'method.consent');

INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('LOGIN_SCA', 8, FALSE, NULL, NULL, TRUE, 5, TRUE, TRUE, 'method.loginSca');

INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('APPROVAL_SCA', 9, FALSE, NULL, NULL, TRUE, 5, TRUE, TRUE, 'method.approvalSca');

```

