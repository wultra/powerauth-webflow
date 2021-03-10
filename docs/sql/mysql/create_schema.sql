-- Table oauth_client_details stores details about OAuth2 client applications.
-- Every Web Flow client application should have a record in this table.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/client/JdbcClientDetailsService.java
CREATE TABLE oauth_client_details (
  client_id               VARCHAR(256) PRIMARY KEY, -- OAuth 2.0 protocol client ID.
  resource_ids            VARCHAR(256),             -- Identifiers of the OAuth 2.0 resource servers.
  client_secret           VARCHAR(256),             -- OAuth 2.0 protocol client secret.
  scope                   VARCHAR(256),             -- OAuth 2.0 scopes, comma-separated values.
  authorized_grant_types  VARCHAR(256),             -- OAuth 2.0 authorization grant types, comma-separated values.
  web_server_redirect_uri VARCHAR(256),             -- OAuth 2.0 redirect URIs, comma-separated values.
  authorities             VARCHAR(256),             -- OAuth 2.0 resource grant authorities.
  access_token_validity   INTEGER,                  -- Validity of the OAuth 2.0 access tokens, in seconds.
  refresh_token_validity  INTEGER,                  -- Validity of the OAuth 2.0 refresh tokens, in seconds.
  additional_information  VARCHAR(4096),            -- Field reserved for additional information about the client.
  autoapprove             VARCHAR(256)              -- Flag indicating if scopes should be automatically approved.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table oauth_client_token stores OAuth2 tokens for retrieval by client applications.
-- See: https://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/client/token/JdbcClientTokenServices.html
CREATE TABLE oauth_client_token (
  authentication_id VARCHAR(256) PRIMARY KEY,       -- Authentication ID related to client token.
  token_id          VARCHAR(256),                   -- Token ID.
  token             LONG VARBINARY,                 -- Token value.
  user_name         VARCHAR(256),                   -- Username, identification of the user.
  client_id         VARCHAR(256)                    -- OAuth 2.0 Client ID.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table oauth_access_token stores OAuth2 access tokens.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/token/store/JdbcTokenStore.java
CREATE TABLE oauth_access_token (
  authentication_id VARCHAR(256) PRIMARY KEY,       -- Authentication ID related to access token.
  token_id          VARCHAR(256),                   -- Token ID.
  token             LONG VARBINARY,                 -- Token value.
  user_name         VARCHAR(256),                   -- Username, identification of the user.
  client_id         VARCHAR(256),                   -- OAuth 2.0 Client ID.
  authentication    LONG VARBINARY,                 -- Encoded authentication details.
  refresh_token     VARCHAR(256)                    -- Refresh token ID.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table oauth_access_token stores OAuth2 refresh tokens.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/token/store/JdbcTokenStore.java
CREATE TABLE oauth_refresh_token (
  token_id       VARCHAR(256),                      -- Refresh token ID.
  token          LONG VARBINARY,                    -- Token value.
  authentication LONG VARBINARY                     -- Encoded authentication details.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table oauth_code stores data for the OAuth2 authorization code grant.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/code/JdbcAuthorizationCodeServices.java
CREATE TABLE oauth_code (
  code           VARCHAR(256),                      -- OAuth 2.0 protocol "codes".
  authentication LONG VARBINARY                     -- Encoded authentication details.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table wf_operation_session maps operations to HTTP sessions.
-- Table is needed for handling of concurrent operations.
CREATE TABLE wf_operation_session (
  operation_id              VARCHAR(256) PRIMARY KEY NOT NULL,  -- Operation ID.
  http_session_id           VARCHAR(256) NOT NULL,              -- HTTP session ID related to given operation.
  operation_hash            VARCHAR(256),                       -- Hash of the operation ID.
  websocket_session_id      VARCHAR(32),                        -- WebSocket Session ID.
  client_ip_address         VARCHAR(32),                        -- Client IP address, if available.
  result                    VARCHAR(32) NOT NULL,               -- Result of the operation, stored in the session.
  timestamp_created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Timestamp of the record creation.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table wf_afs_config is used to configure anti-fraud system parameters.
CREATE TABLE wf_afs_config (
  config_id                 VARCHAR(256) PRIMARY KEY NOT NULL,  -- AFS config ID.
  js_snippet_url            VARCHAR(256) NOT NULL,              -- URL of the AFS JavaScript snippet (relative to application or absolute).
  parameters                TEXT                                -- Additional AFS snippet parameters.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table wf_certificate_verification is used for storing information about verified client TLS certificates.
CREATE TABLE wf_certificate_verification (
  operation_id               VARCHAR(256) NOT NULL,                        -- Operation ID associated with the certificate verification.
  auth_method                VARCHAR(32) NOT NULL,                         -- Authentication method in which the certificate authentication was used.
  client_certificate_issuer  VARCHAR(4096) NOT NULL,                       -- Certificate attribute representing the certificate issuer.
  client_certificate_subject VARCHAR(4096) NOT NULL,                       -- Certificate attribute representing the certificate subject.
  client_certificate_sn      VARCHAR(256) NOT NULL,                        -- Certificate attribute representing the certificate serial number.
  operation_data             TEXT NOT NULL,                                -- Operation data that were included in the certificate authentication request.
  timestamp_verified         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Timestamp of the certificate verification.
  PRIMARY KEY (operation_id, auth_method)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_auth_method stores configuration of authentication methods.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_auth_method (
  auth_method        VARCHAR(32) PRIMARY KEY NOT NULL, -- Name of the authentication method: APPROVAL_SCA, CONSENT, INIT, LOGIN_SCA, POWERAUTH_TOKEN, SHOW_OPERATION_DETAIL, SMS_KEY, USER_ID_ASSIGN, USERNAME_PASSWORD_AUTH
  order_number       INTEGER NOT NULL,                 -- Order of the authentication method, incrementing value, starts with 1.
  check_user_prefs   BOOLEAN NOT NULL,                 -- Indication if the authentication method requires checking the user preference first.
  user_prefs_column  INTEGER,                          -- In case the previous column is 'true', this is pointer to the user preferences configuration column index.
  user_prefs_default BOOLEAN DEFAULT FALSE,            -- Default value of the user preferences, in case the per-user preference is not found.
  check_auth_fails   BOOLEAN NOT NULL,                 -- Indication if the methods can fail, and hence the fail count must be checked.
  max_auth_fails     INTEGER,                          -- Maximum allowed number of authentication fails.
  has_user_interface BOOLEAN DEFAULT FALSE,            -- Indication of if the given method has any user interface in the web flow.
  has_mobile_token   BOOLEAN DEFAULT FALSE,            -- Indication of if the given authentication method has mobile token as a part of the flow.
  display_name_key   VARCHAR(32)                       -- Localization key to the display name of the authentication method.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_operation_config stores configuration of operations.
-- Each operation type (defined by operation_name) has a related mobile token template and configuration.
CREATE TABLE ns_operation_config (
  operation_name            VARCHAR(32) PRIMARY KEY NOT NULL, -- Name of the operation, for example "login" or "approve_payment".
  template_version          CHAR NOT NULL,                    -- Version of the template, used for data signing base.
  template_id               INTEGER NOT NULL,                 -- ID of the template, used for data signing base.
  mobile_token_enabled      BOOLEAN DEFAULT FALSE NOT NULL,   -- Flag indicating if the mobile token is enabled for this operation type.
  mobile_token_mode         VARCHAR(256) NOT NULL,            -- Configuration of mobile token for this operation, for example, if 1FA or 2FA is supported, and which 2FA variants. The field contains a serialized JSON with configuration.
  afs_enabled               BOOLEAN NOT NULL DEFAULT FALSE,   -- Flag indicating if AFS system is enabled.
  afs_config_id             VARCHAR(256),                     -- Configuration of AFS system.
  FOREIGN KEY ns_operation_afs_fk (afs_config_id) REFERENCES wf_afs_config (config_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_organization stores definitions of organizations related to the operations.
-- At least one default organization must be configured.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_organization (
  organization_id          VARCHAR(256) PRIMARY KEY NOT NULL, -- ID of organization.
  display_name_key         VARCHAR(256),                      -- Localization key for the organization display name.
  is_default               BOOLEAN NOT NULL,                  -- Flag indicating if this organization is the default.
  order_number             INTEGER NOT NULL,                  -- Ordering column for this organization, incrementing value, starts with 1.
  default_credential_name  VARCHAR(256),                      -- Default name of credential definition for authentication using Next Step.
  default_otp_name         VARCHAR(256)                       -- Default name of OTP definition for authentication using Next Step.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_step_definition stores definitions of authentication/authorization steps.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_step_definition (
  step_definition_id       INTEGER PRIMARY KEY NOT NULL,      -- Step definition ID.
  operation_name           VARCHAR(32) NOT NULL,              -- Operation name for which this step definition is valid.
  operation_type           VARCHAR(32) NOT NULL,              -- Type of the operation change: CREATE or UPDATE
  request_auth_method      VARCHAR(32),                       -- Operation authentication method that was selected by the user or developer.
  request_auth_step_result VARCHAR(32),                       -- Result of the authentication method execution: CONFIRMED, CANCELED, AUTH_METHOD_FAILED, AUTH_FAILED, AUTH_METHOD_CHOSEN, AUTH_METHOD_DOWNGRADE
  response_priority        INTEGER NOT NULL,                  -- Response priority (ordering column).
  response_auth_method     VARCHAR(32),                       -- Response with the authentication method that should be applied next.
  response_result          VARCHAR(32) NOT NULL,              -- Result of the operation: CONTINUE, FAILED, or DONE
  FOREIGN KEY ns_request_auth_method_fk (request_auth_method) REFERENCES ns_auth_method (auth_method),
  FOREIGN KEY ns_response_auth_method_fk (response_auth_method) REFERENCES ns_auth_method (auth_method)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_application stores Next Step applications.
CREATE TABLE ns_application (
  application_id         INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,  -- Next Step application ID (autogenerated).
  name                   VARCHAR(256) NOT NULL,                        -- Application name used for identification.
  description            VARCHAR(256),                                 -- Description of the application.
  status                 VARCHAR(32) NOT NULL,                         -- Application status: ACTIVE, REMOVED.
  organization_id        VARCHAR(256),                                 -- Organization this application belongs to.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,          -- Timestamp when application was created.
  timestamp_last_updated TIMESTAMP,                                    -- Timestamp when application was last updated.
  CONSTRAINT ns_application_organization_fk FOREIGN KEY (organization_id) REFERENCES ns_organization (organization_id)
);

-- Table ns_credential_policy stores credential policies.
CREATE TABLE ns_credential_policy (
  credential_policy_id       INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- Credential policy ID (autogenerated).
  name                       VARCHAR(256) NOT NULL,                        -- Credential policy name used for identification.
  description                VARCHAR(256),                                 -- Description of the credential policy.
  status                     VARCHAR(32) NOT NULL,                         -- Credential policy status: ACTIVE, REMOVED.
  username_length_min        INTEGER,                                      -- Minimum length of username.
  username_length_max        INTEGER,                                      -- Maximum length of username.
  username_allowed_pattern   VARCHAR(256),                                 -- Allowed pattern for username (regular expression).
  credential_length_min      INTEGER,                                      -- Minimum length of credential value.
  credential_length_max      INTEGER,                                      -- Maximum length of credential value.
  limit_soft                 INTEGER,                                      -- Soft limit of failed attempts.
  limit_hard                 INTEGER,                                      -- Hard limit of failed attempts.
  check_history_count        INTEGER DEFAULT 0 NOT NULL,                   -- Number of historical credential values to check.
  rotation_enabled           INTEGER DEFAULT 0 NOT NULL,                   -- Whether credential rotation is enabled.
  rotation_days              INTEGER,                                      -- Number of days for credential rotation.
  username_gen_algorithm     VARCHAR(256) DEFAULT 'DEFAULT' NOT NULL,      -- Algorithm used for generating the username.
  username_gen_param         VARCHAR(4096) NOT NULL,                       -- Parameters used when generating the username.
  credential_gen_algorithm   VARCHAR(256) DEFAULT 'DEFAULT' NOT NULL,      -- Algorithm used for generating the credential.
  credential_gen_param       VARCHAR(4096) NOT NULL,                       -- Parameters used when generating the credential.
  credential_val_param       VARCHAR(4096) NOT NULL,                       -- Parameters used when validating the credential.
  timestamp_created          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,          -- Timestamp when policy was created.
  timestamp_last_updated     TIMESTAMP                                     -- Timestamp when policy was last updated.
);

-- Table ns_credential_policy stores one time password policies.
CREATE TABLE ns_otp_policy (
  otp_policy_id          INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- One time password policy ID (autogenerated).
  name                   VARCHAR(256) NOT NULL,                       -- One time password policy name used for identification.
  description            VARCHAR(256),                                -- Description of the one time password policy.
  status                 VARCHAR(32) NOT NULL,                        -- One time password policy status: ACTIVE, REMOVED.
  length                 INTEGER NOT NULL,                            -- One time password length.
  attempt_limit          INTEGER,                                     -- Maximum number of authentication attempts.
  expiration_time        INTEGER,                                     -- One time password expiration time.
  gen_algorithm          VARCHAR(256) DEFAULT 'DEFAULT' NOT NULL,     -- Algorithm used for generating the one time password.
  gen_param              VARCHAR(4096) NOT NULL,                      -- Parameters used when generating the OTP.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when policy was created.
  timestamp_last_updated TIMESTAMP                                    -- Timestamp when policy was last updated.
);

-- Table ns_user_identity stores user identities.
CREATE TABLE ns_user_identity (
  user_id                VARCHAR(256) NOT NULL PRIMARY KEY,           -- User identity identifier (not autogenerated).
  status                 VARCHAR(32) NOT NULL,                        -- User identity status: ACTIVE, BLOCKED, REMOVED.
  extras                 VARCHAR(256),                                -- Extra attributes with data related to user identity.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when user identity was created.
  timestamp_last_updated TIMESTAMP                                    -- Timestamp when user identity was last updated.
);

-- Table ns_user_contact stores contact information for user identities.
CREATE TABLE ns_user_contact (
  user_contact_id        INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- User contact identifier (autogenerated).
  user_id                VARCHAR(256) NOT NULL,                       -- User identity identifier.
  name                   VARCHAR(256) NOT NULL,                       -- User contact name used for identification.
  type                   VARCHAR(32) NOT NULL,                        -- User contact type: PHONE, EMAIL, OTHER.
  value                  VARCHAR(256),                                -- User contact value.
  is_primary             INTEGER DEFAULT 0 NOT NULL,                  -- Whether contact is primary.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when contact was created.
  timestamp_last_updated TIMESTAMP,                                   -- Timestamp when contact was last updated.
  CONSTRAINT ns_user_contact_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_user_identity stores history for user identities.
CREATE TABLE ns_user_identity_history (
  user_identity_history_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- User identity history identifier (autogenerated).
  user_id                  VARCHAR(256) NOT NULL,                       -- User identity identifier.
  status                   VARCHAR(32) NOT NULL,                        -- User identity status: ACTIVE, BLOCKED, REMOVED.
  roles                    VARCHAR(256),                                -- Assigned user roles.
  extras                   VARCHAR(256),                                -- Extra attributes with data related to user identity.
  timestamp_created        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when user identity snapshot was created.
  CONSTRAINT ns_user_identity_history_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_role stores user role definitions.
CREATE TABLE ns_role (
  role_id                INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- Role identifier (autogenerated).
  name                   VARCHAR(256) NOT NULL,                       -- Role name used for identification.
  description            VARCHAR(256),                                -- Description of role.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when role was created.
  timestamp_last_updated TIMESTAMP                                    -- Timestamp when role was last updated.
);

-- Table ns_user_role stores assignment of roles to user identities.
CREATE TABLE ns_user_role (
  user_role_id             INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- User role identifier (autogenerated).
  user_id                  VARCHAR(256) NOT NULL,                       -- User identity identifier.
  role_id                  INTEGER NOT NULL,                            -- Role identifier.
  status                   VARCHAR(32) NOT NULL,                        -- User role status: ACTIVE, REMOVED.
  timestamp_created        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when user role was created.
  timestamp_last_updated   TIMESTAMP,                                   -- Timestamp when user role was last updated.
  CONSTRAINT ns_role_identity_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id),
  CONSTRAINT ns_user_role_fk FOREIGN KEY (role_id) REFERENCES ns_role (role_id)
);

-- Table ns_user_alias stores user aliases.
CREATE TABLE ns_user_alias (
  user_alias_id            INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- User alias identifier (autogenerated).
  user_id                  VARCHAR(256) NOT NULL,                       -- User identity identifier.
  name                     VARCHAR(256) NOT NULL,                       -- User alias name used for identification.
  value                    VARCHAR(256) NOT NULL,                       -- User alias value.
  status                   VARCHAR(32) NOT NULL,                        -- User alias status: ACTIVE, REMOVED.
  extras                   VARCHAR(256),                                -- Extra attributes with data related to user alias.
  timestamp_created        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when user alias was created.
  timestamp_last_updated   TIMESTAMP,                                   -- Timestamp when user alias was last updated.
  CONSTRAINT ns_user_alias_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_hashing_config stores configuration of hashing algorithms.
CREATE TABLE ns_hashing_config (
  hashing_config_id        INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- Hashing configuration identifier (autogenerated).
  name                     VARCHAR(256) NOT NULL,                       -- Hashing configuration name used for identification.
  algorithm                VARCHAR(256) NOT NULL,                       -- Hashing algorithm name.
  status                   VARCHAR(32) NOT NULL,                        -- Hashing configuration status: ACTIVE, REMOVED.
  parameters               VARCHAR(256),                                -- Hashing algorithm parameters.
  timestamp_created        TIMESTAMP                                    -- Timestamp when hashing configuration was created.
);

-- Table ns_credential_definition stores definitions of credentials with reference to credential policies and applications.
CREATE TABLE ns_credential_definition (
  credential_definition_id   INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- Credential definition identifier (autogenerated).
  name                       VARCHAR(256) NOT NULL,                       -- Credential definition name used for identification.
  description                VARCHAR(256),                                -- Description of the credential definition.
  application_id             INTEGER NOT NULL,                            -- Application identifier.
  credential_policy_id       INTEGER NOT NULL,                            -- Credential policy identifier.
  category                   VARCHAR(32) NOT NULL,                        -- Credential category: PASSWORD, PIN, OTHER.
  encryption_enabled         INTEGER DEFAULT 0 NOT NULL,                  -- Whether encryption of stored credentials is enabled.
  encryption_algorithm       VARCHAR(256),                                -- Algorithm used for stored credential encryption.
  hashing_enabled            INTEGER DEFAULT 0 NOT NULL,                  -- Whether credential hashing is enabled.
  hashing_config_id          INTEGER,                                     -- Algorithm used for credential hashing.
  e2e_encryption_enabled     INTEGER DEFAULT 0 NOT NULL,                  -- Whether end to end encryption of credential is enabled.
  data_adapter_proxy_enabled INTEGER DEFAULT 0 NOT NULL,                  -- Whether credential API calls should be proxied through Data Adapter.
  status                     VARCHAR(32) NOT NULL,                        -- Credential definition status: ACTIVE, REMOVED.
  timestamp_created          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when credential definition was created.
  timestamp_last_updated     TIMESTAMP,                                   -- Timestamp when credential definition was last updated.
  CONSTRAINT ns_credential_application_fk FOREIGN KEY (application_id) REFERENCES ns_application (application_id),
  CONSTRAINT ns_credential_policy_fk FOREIGN KEY (credential_policy_id) REFERENCES ns_credential_policy (credential_policy_id),
  CONSTRAINT ns_credential_hash_fk FOREIGN KEY (hashing_config_id) REFERENCES ns_hashing_config (hashing_config_id)
);

-- Table ns_otp_definition stores definitions of one time passwords with reference to credential policies and applications.
CREATE TABLE ns_otp_definition (
  otp_definition_id          INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- One time password definition identifier (autogenerated).
  name                       VARCHAR(256) NOT NULL,                       -- One time password definition name used for identification.
  description                VARCHAR(256),                                -- Description of the one time password definition.
  application_id             INTEGER NOT NULL,                            -- Application identifier.
  otp_policy_id              INTEGER NOT NULL,                            -- One time password policy identifier.
  encryption_enabled         INTEGER DEFAULT 0 NOT NULL,                  -- Whether encryption of stored one time passwords is enabled.
  encryption_algorithm       VARCHAR(256),                                -- Algorithm used for stored one time password encryption.
  data_adapter_proxy_enabled INTEGER DEFAULT 0 NOT NULL,                  -- Whether one time password API calls should be proxied through Data Adapter.
  status                     VARCHAR(32) NOT NULL,                        -- One time password definition status: ACTIVE, REMOVED.
  timestamp_created          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when one time password definition was created.
  timestamp_last_updated     TIMESTAMP,                                   -- Timestamp when one time password definition was last updated.
  CONSTRAINT ns_otp_application_fk FOREIGN KEY (application_id) REFERENCES ns_application (application_id),
  CONSTRAINT ns_otp_policy_fk FOREIGN KEY (otp_policy_id) REFERENCES ns_otp_policy (otp_policy_id)
);

-- Table ns_credential_storage stores credential values, counters and other data related to credentials.
CREATE TABLE ns_credential_storage (
  credential_id                    VARCHAR(256) NOT NULL PRIMARY KEY,   -- Credential identifier (generated by application as UUID).
  credential_definition_id         INTEGER NOT NULL,                    -- Credential definition identifier.
  user_id                          VARCHAR(256) NOT NULL,               -- User identity identifier.
  type                             VARCHAR(32) NOT NULL,                -- Credential type: PERMANENT, TEMPORARY.
  user_name                        VARCHAR(256),                        -- Username.
  value                            VARCHAR(256) NOT NULL,               -- Credential value.
  status                           VARCHAR(32) NOT NULL,                -- Credential status: ACTIVE, BLOCKED_TEMPORARY, BLOCKED_PERMANENT, REMOVED.
  attempt_counter                  INTEGER DEFAULT 0 NOT NULL,          -- Attempt counter for both successful and failed attempts.
  failed_attempt_counter_soft      INTEGER DEFAULT 0 NOT NULL,          -- Soft failed attempt counter.
  failed_attempt_counter_hard      INTEGER DEFAULT 0 NOT NULL,          -- Hard failed attempt counter.
  timestamp_created                TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when credential was created.
  timestamp_expires                TIMESTAMP,                           -- Timestamp when credential expires.
  timestamp_blocked                TIMESTAMP,                           -- Timestamp when credential was blocked.
  timestamp_last_updated           TIMESTAMP,                           -- Timestamp when credential was last updated.
  timestamp_last_credential_change TIMESTAMP,                           -- Timestamp when credential value was last changed.
  CONSTRAINT ns_credential_definition_fk FOREIGN KEY (credential_definition_id) REFERENCES ns_credential_definition (credential_definition_id),
  CONSTRAINT ns_credential_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_credential_history stores historical values of credentials.
CREATE TABLE ns_credential_history (
  credential_history_id       INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- Credential history identifier (autogenerated).
  credential_definition_id    INTEGER NOT NULL,                            -- Credential identifier.
  user_id                     VARCHAR(256) NOT NULL,                       -- User identity identifier.
  user_name                   VARCHAR(256),                                -- Username.
  value                       VARCHAR(256) NOT NULL,                       -- Credential value.
  timestamp_created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when credential was created.
  CONSTRAINT ns_credential_history_definition_fk FOREIGN KEY (credential_definition_id) REFERENCES ns_credential_definition (credential_definition_id),
  CONSTRAINT ns_credential_history_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_otp_storage stores one time password values, counters and other data related to one time passwords.
CREATE TABLE ns_otp_storage (
  otp_id                      VARCHAR(256) NOT NULL PRIMARY KEY,   -- One time password identifier (generated by application as UUID).
  otp_definition_id           INTEGER NOT NULL,                    -- One time password definition identifier.
  user_id                     VARCHAR(256),                        -- User identifier.
  credential_definition_id    INTEGER,                             -- Credential definition identifier used when updating failed counter.
  operation_id                VARCHAR(256),                        -- Operation identifier.
  value                       VARCHAR(256),                        -- One time password value.
  salt                        VARBINARY(16),                       -- Cryptographic salt used when generating one time password.
  status                      VARCHAR(32) NOT NULL,                -- One time password status: ACTIVE, USED, BLOCKED, REMOVED.
  otp_data                    TEXT,                                -- Data used for generating one time password.
  attempt_counter             INTEGER DEFAULT 0 NOT NULL,          -- One time password attempt counter.
  failed_attempt_counter      INTEGER DEFAULT 0 NOT NULL,          -- One time password failed attempt counter.
  timestamp_created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when one time password was created.
  timestamp_verified          TIMESTAMP,                           -- Timestamp when one time password was verified.
  timestamp_blocked           TIMESTAMP,                           -- Timestamp when one time password was verified.
  timestamp_expires           TIMESTAMP,                           -- Timestamp when one time password expires.
  CONSTRAINT ns_otp_definition_fk FOREIGN KEY (otp_definition_id) REFERENCES ns_otp_definition (otp_definition_id)
);

-- Table ns_operation stores details of Web Flow operations.
-- Only the last status is stored in this table, changes of operations are stored in table ns_operation_history.
CREATE TABLE ns_operation (
  operation_id                  VARCHAR(256) PRIMARY KEY NOT NULL,   -- ID of a specific operation instance, random value in the UUID format or any value that external system decides to set as the operation ID when creating the operation.
  operation_name                VARCHAR(32) NOT NULL,                -- Name of the operation, represents a type of the operation, for example, "login" or "approve_payment".
  operation_data                TEXT NOT NULL,                       -- Signing data of the operation.
  operation_form_data           TEXT,                                -- Structured data of the operation that are displayed to the end user.
  application_id                VARCHAR(256),                        -- ID of the application that initiated the operation, usually OAuth 2.0 client ID.
  application_name              VARCHAR(256),                        -- Displayable name of the application that initiated the operation.
  application_description       VARCHAR(256),                        -- Displayable description of the application that initiated the operation.
  application_original_scopes   VARCHAR(256),                        -- Original OAuth 2.0 scopes used by the application that initiated the operation.
  application_extras            TEXT,                                -- Any additional information related to the application that initiated the operation.
  user_id                       VARCHAR(256),                        -- Associated user ID.
  organization_id               VARCHAR(256),                        -- Associated organization ID.
  user_account_status           VARCHAR(32),                         -- Status of the user account while initiated the operation - ACTIVE, NOT_ACTIVE.
  external_operation_name       VARCHAR(32),                         -- External operation name, which can further specify the operation purpose.
  external_transaction_id       VARCHAR(256),                        -- External transaction ID, for example ID of a payment in a transaction system.
  result                        VARCHAR(32),                         -- Operation result - CONTINUE, FAILED, DONE.
  timestamp_created             TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when this operation was created.
  timestamp_expires             TIMESTAMP,                           -- Timestamp of the expiration of the operation.
  FOREIGN KEY ns_operation_organization_fk (organization_id) REFERENCES ns_organization (organization_id),
  FOREIGN KEY ns_operation_config_fk (operation_name) REFERENCES ns_operation_config (operation_name)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_authentication stores authentication attempts.
CREATE TABLE ns_authentication (
  authentication_id           VARCHAR(256) NOT NULL PRIMARY KEY,           -- Authentication identifier (autogenerated).
  user_id                     VARCHAR(256),                                -- User identity identifier.
  type                        VARCHAR(32) NOT NULL,                        -- Authentication type: CREDENTIAL, OTP, CREDENTIAL_OTP.
  credential_id               VARCHAR(256),                                -- Credential identifier.
  otp_id                      VARCHAR(256),                                -- One time password identifier.
  operation_id                VARCHAR(256),                                -- Operation identifier.
  result                      VARCHAR(32) NOT NULL,                        -- Overall authentication result.
  result_credential           VARCHAR(32),                                 -- Authentication result for credential authentication.
  result_otp                  VARCHAR(32),                                 -- Authentication result for one time password authentication.
  timestamp_created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when authentication record was created.
  CONSTRAINT ns_auth_credential_fk FOREIGN KEY (credential_id) REFERENCES ns_credential_storage (credential_id),
  CONSTRAINT ns_auth_otp_fk FOREIGN KEY (otp_id) REFERENCES ns_otp_storage (otp_id),
  CONSTRAINT ns_auth_operation_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id)
);

-- Table ns_operation_history stores all changes of operations.
CREATE TABLE ns_operation_history (
  operation_id                VARCHAR(256) NOT NULL,               -- Operation ID.
  result_id                   INTEGER NOT NULL,                    -- Result ordering index identifier, incrementing value, starts with 1.
  request_auth_method         VARCHAR(32) NOT NULL,                -- Authentication method used for the step.
  request_auth_instruments    VARCHAR(256),                        -- Which specific instruments were used for the step. Supported values are: PASSWORD, OTP_KEY, POWERAUTH_TOKEN, HW_TOKEN. There can be multiple supported instruments, they are stored encoded in JSON format.
  request_auth_step_result    VARCHAR(32) NOT NULL,                -- Authentication result: CANCELED, AUTH_METHOD_FAILED, AUTH_FAILED, CONFIRMED, AUTH_METHOD_CHOSEN, AUTH_METHOD_DOWNGRADE
  request_params              VARCHAR(4096),                       -- Additional request parameters.
  response_result             VARCHAR(32) NOT NULL,                -- Authentication step result: FAILED, CONTINUE, DONE.
  response_result_description VARCHAR(256),                        -- Additional information about the authentication step result.
  response_steps              VARCHAR(4096),                       -- Information about which methods are allowed in the next step.
  response_timestamp_created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when the record was created.
  response_timestamp_expires  TIMESTAMP,                           -- Timestamp when the operation step should expire.
  chosen_auth_method          VARCHAR(32),                         -- Information about which authentication method was chosen, in case user can chose the authentication method.
  mobile_token_active         BOOLEAN NOT NULL DEFAULT FALSE,      -- Information about if mobile token is active during the particular authentication step, in order to show the mobile token operation at the right time.
  authentication_id           VARCHAR(256),                        -- Reference to the authentication record.
  PRIMARY KEY (operation_id, result_id),
  FOREIGN KEY ns_history_operation_fk (operation_id) REFERENCES ns_operation (operation_id),
  FOREIGN KEY ns_history_auth_method_fk (request_auth_method) REFERENCES ns_auth_method (auth_method),
  FOREIGN KEY ns_history_chosen_method_fk (chosen_auth_method) REFERENCES ns_auth_method (auth_method),
  FOREIGN KEY ns_history_authentication_fk (authentication_id) REFERENCES ns_authentication (authentication_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_operation_afs stores AFS requests and responses.
CREATE TABLE ns_operation_afs (
  afs_action_id               INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT, -- ID of the AFS action.
  operation_id                VARCHAR(256) NOT NULL,                       -- Operation ID.
  request_afs_action          VARCHAR(256) NOT NULL,                       -- Information about requested AFS action.
  request_step_index          INTEGER NOT NULL,                            -- Counter within the specific operation step that is associated with AFS action, e.g. to differentiate multiple authentication attempts. Incrementing value, starts with 1.
  request_afs_extras          VARCHAR(256),                                -- Additional information about AFS action, typically a cookie values used in AFS system.
  response_afs_apply          INTEGER DEFAULT 0 NOT NULL,                  -- Response information about if AFS was applied.
  response_afs_label          VARCHAR(256),                                -- Response AFS label (information about what should the application do).
  response_afs_extras         VARCHAR(256),                                -- Additional information sent in AFS response.
  timestamp_created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp this AFS action was created.
  CONSTRAINT operation_afs_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id)
);

-- Table ns_audit_log stores audit information.
CREATE TABLE ns_audit_log (
  audit_log_id           INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, -- Audit log identifier.
  action                 VARCHAR(256) NOT NULL,                       -- Action which is being audited.
  data                   TEXT,                                        -- Data for the audit record.
  timestamp_created      TIMESTAMP                                    -- Timestamp when audit record was created.
);

-- Table ns_user_prefs stores user preferences.
-- Status of authentication methods is stored in this table per user (methods can be enabled or disabled).
CREATE TABLE ns_user_prefs (
  user_id       VARCHAR(256) PRIMARY KEY NOT NULL, -- User ID.
  auth_method_1 BOOLEAN DEFAULT FALSE,             -- Flag indicating if "authentication method 1" is enabled.
  auth_method_2 BOOLEAN DEFAULT FALSE,             -- Flag indicating if "authentication method 2" is enabled.
  auth_method_3 BOOLEAN DEFAULT FALSE,             -- Flag indicating if "authentication method 3" is enabled.
  auth_method_4 BOOLEAN DEFAULT FALSE,             -- Flag indicating if "authentication method 4" is enabled.
  auth_method_5 BOOLEAN DEFAULT FALSE,             -- Flag indicating if "authentication method 5" is enabled.
  auth_method_1_config VARCHAR(256),               -- Configuration for "authentication method 1".
  auth_method_2_config VARCHAR(256),               -- Configuration for "authentication method 2".
  auth_method_3_config VARCHAR(256),               -- Configuration for "authentication method 3".
  auth_method_4_config VARCHAR(256),               -- Configuration for "authentication method 4".
  auth_method_5_config VARCHAR(256)                -- Configuration for "authentication method 5".
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table da_sms_authorization stores data for SMS OTP authorization.
CREATE TABLE da_sms_authorization (
  message_id           VARCHAR(256) PRIMARY KEY NOT NULL,   -- SMS message ID, ID of SMS OTP.
  operation_id         VARCHAR(256) NOT NULL,               -- Operation ID.
  user_id              VARCHAR(256) NOT NULL,               -- User ID.
  organization_id      VARCHAR(256),                        -- Organization ID.
  operation_name       VARCHAR(32) NOT NULL,                -- Name of the operation that triggered the SMS (login, authorize_payment, ...).
  authorization_code   VARCHAR(32) NOT NULL,                -- Value of the authorization code sent in the SMS.
  salt                 VARBINARY(16) NOT NULL,              -- Salt used for authorization code calculation.
  message_text         TEXT NOT NULL,                       -- Full SMS message text.
  verify_request_count INTEGER,                             -- Number of verification attempts.
  verified             BOOLEAN DEFAULT FALSE,               -- Flag indicating if this SMS OTP was successfully verified.
  timestamp_created    TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when the SMS OTP was generated.
  timestamp_verified   TIMESTAMP,                           -- Timestamp when the SMS OTP was successfully validated.
  timestamp_expires    TIMESTAMP                            -- Timestamp when the SMS OTP expires.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table da_user_credentials stores built-in users for the data adapter
CREATE TABLE da_user_credentials (
  user_id               VARCHAR(128) PRIMARY KEY NOT NULL,  -- User ID. Technical identifier of the user.
  username              VARCHAR(256) NOT NULL,              -- Username, the displayable value that users use to sign in.
  password_hash         VARCHAR(256) NOT NULL,              -- Bcrypt hash of the password.
  family_name           VARCHAR(256) NOT NULL,              -- User family name.
  given_name            VARCHAR(256) NOT NULL,              -- User given name.
  organization_id       VARCHAR(64)  NOT NULL,              -- User organization ID.
  phone_number          VARCHAR(256) NOT NULL               -- Full phone number, should be stored in format that allows easy SMS message sending.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table for the list of consent currently given by a user
CREATE TABLE tpp_consent (
  consent_id            VARCHAR(64) PRIMARY KEY NOT NULL,   -- Consent ID.
  consent_name          VARCHAR(128) NOT NULL,              -- Consent name, localization key or full displayable value.
  consent_text          TEXT NOT NULL,                      -- Consent text, localization key or full displayable value with optional placeholders.
  version               INT NOT NULL                        -- Consent version.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table for the list of changes in consent history by given user
CREATE TABLE tpp_user_consent (
  id                    INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT, -- User given consent ID.
  user_id               VARCHAR(256) NOT NULL,                       -- User ID.
  client_id             VARCHAR(256) NOT NULL,                       -- OAuth 2.0 client ID.
  consent_id            VARCHAR(64) NOT NULL,                        -- Consent ID.
  external_id           VARCHAR(256),                                -- External ID associated with the consent approval, usually the operation ID.
  consent_parameters    TEXT NOT NULL,                               -- Specific parameters that were filled in into the user consent template.
  timestamp_created     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp the consent with given ID was first created.
  timestamp_updated     TIMESTAMP                                    -- Timestamp the consent with given ID was given again before it was revoked (updated, prolonged).
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE tpp_user_consent_history (
  id                    INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT, -- ID of the consent history record.
  user_id               VARCHAR(256) NOT NULL,                       -- User ID.
  client_id             VARCHAR(256) NOT NULL,                       -- Client ID.
  consent_id            VARCHAR(64) NOT NULL,                        -- Consent ID.
  consent_change        VARCHAR(16) NOT NULL,                        -- Type of the consent change: APPROVE, PROLONG, REJECT
  external_id           VARCHAR(256),                                -- External ID that was responsible for this specific consent change, usually the operation ID.
  consent_parameters    TEXT NOT NULL,                               -- Specific parameters that were filled in into the user consent template in this consent change.
  timestamp_created     TIMESTAMP DEFAULT CURRENT_TIMESTAMP          -- Timestamp of the consent change.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE tpp_detail (
  tpp_id                INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT, -- ID of the TPP provider.
  tpp_name              VARCHAR(256) NOT NULL,                       -- Name of the TPP provider.
  tpp_license           VARCHAR(256) NOT NULL,                       -- Information about the TPP license.
  tpp_info              TEXT NULL,                                   -- Additional information about the TPP provider, if available.
  tpp_address           TEXT NULL,                                   -- TPP address, if available.
  tpp_website           TEXT NULL,                                   -- TPP website, if available.
  tpp_phone             VARCHAR(256) NULL,                           -- TPP phone number, if available.
  tpp_email             VARCHAR(256) NULL,                           -- TPP e-mail, if available.
  tpp_logo              BLOB NULL                                    -- TPP logo, if available.
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE tpp_app_detail (
  tpp_id                INTEGER NOT NULL,                            -- TPP ID.
  app_client_id         VARCHAR(256) NOT NULL,                       -- TPP app ID, represented as OAuth 2.0 client ID and connecting the application to OAuth 2.0 credentials.
  app_name              VARCHAR(256) NOT NULL,                       -- TPP app name.
  app_info              TEXT NULL,                                   -- An arbitrary additional info about TPP app, if available.
  app_type              VARCHAR(32) NULL,                            -- Application type, "web" or "native".
  PRIMARY KEY (tpp_id, app_client_id),
  FOREIGN KEY tpp_detail_fk (tpp_id) REFERENCES tpp_detail (tpp_id),
  FOREIGN KEY tpp_client_secret_fk (app_client_id) REFERENCES oauth_client_details (client_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE INDEX wf_operation_hash ON wf_operation_session (operation_hash);
CREATE INDEX wf_websocket_session ON wf_operation_session (websocket_session_id);
CREATE INDEX ns_operation_pending ON ns_operation (user_id, result);
CREATE UNIQUE INDEX ns_operation_afs_unique on ns_operation_afs (operation_id, request_afs_action, request_step_index);
CREATE INDEX wf_certificate_operation ON wf_certificate_verification (operation_id);
CREATE UNIQUE INDEX ns_application_name ON ns_application (name);
CREATE UNIQUE INDEX ns_credential_policy_name ON ns_credential_policy (name);
CREATE UNIQUE INDEX ns_otp_policy_name ON ns_otp_policy (name);
CREATE INDEX ns_user_contact_user_id ON ns_user_contact (user_id);
CREATE INDEX ns_user_contact_query ON ns_user_contact (user_id, name, type);
CREATE INDEX ns_user_identity_status ON ns_user_identity (status);
CREATE INDEX ns_user_identity_created ON ns_user_identity (timestamp_created);
CREATE INDEX ns_user_identity_history_user ON ns_user_identity_history (user_id);
CREATE INDEX ns_user_identity_history_created ON ns_user_identity_history (timestamp_created);
CREATE UNIQUE INDEX ns_role_name ON ns_role (name);
CREATE INDEX ns_user_role_user_id ON ns_user_role (user_id);
CREATE INDEX ns_user_role_role_id ON ns_user_role (role_id);
CREATE INDEX ns_user_alias_user_id ON ns_user_alias (user_id);
CREATE UNIQUE INDEX ns_credential_definition_name ON ns_credential_definition (name);
CREATE UNIQUE INDEX ns_otp_definition_name ON ns_otp_definition (name);
CREATE INDEX ns_credential_storage_user_id ON ns_credential_storage (user_id);
CREATE INDEX ns_credential_storage_status ON ns_credential_storage (status);
CREATE UNIQUE INDEX ns_credential_storage_query1 ON ns_credential_storage (credential_definition_id, user_name);
CREATE UNIQUE INDEX ns_credential_storage_query2 ON ns_credential_storage (user_id, credential_definition_id);
CREATE INDEX ns_credential_storage_query3 ON ns_credential_storage (credential_definition_id, status);
CREATE INDEX ns_credential_history_user_id ON ns_credential_history (user_id);
CREATE INDEX ns_otp_storage_user_id ON ns_otp_storage (user_id);
CREATE INDEX ns_otp_storage_operation_id ON ns_otp_storage (operation_id);
CREATE INDEX ns_authentication_user_id ON ns_authentication (user_id);
CREATE INDEX ns_authentication_operation_id ON ns_authentication (operation_id);
CREATE INDEX ns_authentication_timestamp_created ON ns_authentication (timestamp_created);
CREATE INDEX ns_audit_log_created ON ns_audit_log (timestamp_created);
CREATE UNIQUE INDEX ns_hashing_config_name ON ns_hashing_config (name);
CREATE UNIQUE INDEX ns_user_alias_unique ON ns_user_alias (user_id, name);
CREATE UNIQUE INDEX ns_user_role_unique ON ns_user_role (user_id, role_id);

-- Foreign keys for user identity, to be used only when all user identities are stored in Next Step
ALTER TABLE ns_user_prefs ADD FOREIGN KEY ns_user_prefs_fk (user_id) REFERENCES ns_user_identity (user_id);
ALTER TABLE ns_operation ADD FOREIGN KEY ns_operation_user_fk (user_id) REFERENCES ns_user_identity (user_id);
ALTER TABLE ns_otp_storage ADD FOREIGN KEY ns_otp_user_fk (user_id) REFERENCES ns_user_identity (user_id);
ALTER TABLE ns_authentication ADD FOREIGN KEY ns_auth_user_fk (user_id) REFERENCES ns_user_identity (user_id);
