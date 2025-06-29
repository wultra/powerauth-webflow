--
--  Create sequences.
--
CREATE SEQUENCE tpp_detail_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE tpp_user_consent_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE tpp_user_consent_history_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_operation_afs_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_application_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_credential_policy_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_otp_policy_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_user_contact_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_user_identity_history_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_role_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_user_role_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_user_alias_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_hashing_config_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_credential_definition_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_otp_definition_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;
CREATE SEQUENCE ns_credential_history_seq MINVALUE 1 INCREMENT BY 1 START WITH 1 CACHE 20;

-- Table oauth2_authorization stores information about OAuth 2.1 authorizations
-- Source: https://github.com/spring-projects/spring-authorization-server/blob/main/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql
CREATE TABLE oauth2_authorization (
    id VARCHAR(100) NOT NULL,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000) DEFAULT NULL,
    attributes TEXT DEFAULT NULL,
    state VARCHAR(500) DEFAULT NULL,
    authorization_code_value TEXT DEFAULT NULL,
    authorization_code_issued_at TIMESTAMP DEFAULT NULL,
    authorization_code_expires_at TIMESTAMP DEFAULT NULL,
    authorization_code_metadata TEXT DEFAULT NULL,
    access_token_value TEXT DEFAULT NULL,
    access_token_issued_at TIMESTAMP DEFAULT NULL,
    access_token_expires_at TIMESTAMP DEFAULT NULL,
    access_token_metadata TEXT DEFAULT NULL,
    access_token_type VARCHAR(100) DEFAULT NULL,
    access_token_scopes VARCHAR(1000) DEFAULT NULL,
    oidc_id_token_value TEXT DEFAULT NULL,
    oidc_id_token_issued_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_expires_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_metadata TEXT DEFAULT NULL,
    refresh_token_value TEXT DEFAULT NULL,
    refresh_token_issued_at TIMESTAMP DEFAULT NULL,
    refresh_token_expires_at TIMESTAMP DEFAULT NULL,
    refresh_token_metadata TEXT DEFAULT NULL,
    user_code_value TEXT DEFAULT NULL,
    user_code_issued_at TIMESTAMP DEFAULT NULL,
    user_code_expires_at TIMESTAMP DEFAULT NULL,
    user_code_metadata TEXT DEFAULT NULL,
    device_code_value TEXT DEFAULT NULL,
    device_code_issued_at TIMESTAMP DEFAULT NULL,
    device_code_expires_at TIMESTAMP DEFAULT NULL,
    device_code_metadata TEXT DEFAULT NULL,
    PRIMARY KEY (id)
);

-- Table oauth2_registered_client stores information about OAuth 2.1 clients
-- Source: https://github.com/spring-projects/spring-authorization-server/blob/main/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql
CREATE TABLE oauth2_registered_client (
    id VARCHAR(100) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at TIMESTAMP DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX oauth2_client_unique ON oauth2_registered_client (client_id);

-- Table oauth2_registered_client stores information about OAuth 2.1 consents
-- Source: https://github.com/spring-projects/spring-authorization-server/blob/main/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql
CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

-- Table wf_operation_session maps operations to HTTP sessions.
-- Table is needed for handling of concurrent operations.
CREATE TABLE wf_operation_session (
  operation_id              VARCHAR(256) PRIMARY KEY NOT NULL,
  http_session_id           VARCHAR(256) NOT NULL,
  operation_hash            VARCHAR(256),
  websocket_session_id      VARCHAR(32),
  client_ip_address         VARCHAR(32),
  result                    VARCHAR(32) NOT NULL,
  timestamp_created         TIMESTAMP
);

-- Table wf_afs_config is used to configure anti-fraud system parameters.
CREATE TABLE wf_afs_config (
  config_id                 VARCHAR(256) PRIMARY KEY NOT NULL,
  js_snippet_url            VARCHAR(256) NOT NULL,
  parameters                TEXT
);

-- Table wf_certificate_verification is used for storing information about verified client TLS certificates.
CREATE TABLE wf_certificate_verification (
  operation_id               VARCHAR(256) NOT NULL,                        -- Operation ID associated with the certificate verification.
  auth_method                VARCHAR(32) NOT NULL,                         -- Authentication method in which the certificate authentication was used.
  client_certificate_issuer  VARCHAR(4000) NOT NULL,                       -- Certificate attribute representing the certificate issuer.
  client_certificate_subject VARCHAR(4000) NOT NULL,                       -- Certificate attribute representing the certificate subject.
  client_certificate_sn      VARCHAR(256) NOT NULL,                        -- Certificate attribute representing the certificate serial number.
  operation_data             TEXT NOT NULL,                                -- Operation data that were included in the certificate authentication request.
  timestamp_verified         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Timestamp of the certificate verification.
  CONSTRAINT wf_certificate_verification_pk PRIMARY KEY (operation_id, auth_method)
);

-- Table ns_auth_method stores configuration of authentication methods.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_auth_method (
  auth_method        VARCHAR(32) PRIMARY KEY NOT NULL, -- Name of the authentication method: APPROVAL_SCA, CONSENT, INIT, LOGIN_SCA, POWERAUTH_TOKEN, SHOW_OPERATION_DETAIL, SMS_KEY, USER_ID_ASSIGN, USERNAME_PASSWORD_AUTH, OTP_CODE.
  order_number       INTEGER NOT NULL,                 -- Order of the authentication method, incrementing value, starts with 1.
  check_user_prefs   BOOLEAN DEFAULT FALSE NOT NULL,   -- Indication if the authentication method requires checking the user preference first.
  user_prefs_column  INTEGER,                          -- In case the previous column is 'true', this is pointer to the user preferences configuration column index.
  user_prefs_default BOOLEAN DEFAULT FALSE,            -- Default value of the user preferences, in case the per-user preference is not found.
  check_auth_fails   BOOLEAN DEFAULT FALSE NOT NULL,   -- Indication if the methods can fail, and hence the fail count must be checked.
  max_auth_fails     INTEGER,                          -- Maximum allowed number of authentication fails.
  has_user_interface BOOLEAN DEFAULT FALSE,            -- Indication of if the given method has any user interface in the web flow.
  has_mobile_token   BOOLEAN DEFAULT FALSE,            -- Indication of if the given authentication method has mobile token as a part of the flow.
  display_name_key   VARCHAR(32)                       -- Localization key to the display name of the authentication method.
);

-- Table ns_operation_config stores configuration of operations.
-- Each operation type (defined by operation_name) has a related mobile token template and configuration.
CREATE TABLE ns_operation_config (
  operation_name            VARCHAR(32) PRIMARY KEY NOT NULL, -- Name of the operation, for example "login" or "authorize_payment".
  template_version          VARCHAR(1) NOT NULL,              -- Version of the template, used for data signing base.
  template_id               INTEGER NOT NULL,                 -- ID of the template, used for data signing base.
  mobile_token_enabled      BOOLEAN DEFAULT FALSE NOT NULL,   -- Flag indicating if the mobile token is enabled for this operation type.
  mobile_token_mode         VARCHAR(256) NOT NULL,            -- Configuration of mobile token for this operation, for example, if 1FA or 2FA is supported, and which 2FA variants. The field contains a serialized JSON with configuration.
  afs_enabled               BOOLEAN DEFAULT FALSE NOT NULL,   -- Flag indicating if AFS system is enabled.
  afs_config_id             VARCHAR(256),                     -- Configuration of AFS system.
  expiration_time           INTEGER,                          -- Expiration time in seconds, which overrides global Next Step configuration.
  CONSTRAINT ns_operation_config_afs_fk FOREIGN KEY (afs_config_id) REFERENCES wf_afs_config (config_id)
);

-- Table ns_operation_method_config stores configuration of authentication methods per operation name.
CREATE TABLE ns_operation_method_config (
  operation_name     VARCHAR(32) NOT NULL,             -- Name of the operation, for example "login" or "authorize_payment".
  auth_method        VARCHAR(32) NOT NULL,             -- Name of the authentication method: APPROVAL_SCA, CONSENT, INIT, LOGIN_SCA, POWERAUTH_TOKEN, SHOW_OPERATION_DETAIL, SMS_KEY, USER_ID_ASSIGN, USERNAME_PASSWORD_AUTH, OTP_CODE.
  max_auth_fails     INTEGER NOT NULL,                 -- Maximum allowed number of authentication fails.
  CONSTRAINT ns_operation_method_pk PRIMARY KEY (operation_name, auth_method),
  CONSTRAINT ns_operation_method_fk1 FOREIGN KEY (operation_name) REFERENCES ns_operation_config (operation_name),
  CONSTRAINT ns_operation_method_fk2 FOREIGN KEY (auth_method) REFERENCES ns_auth_method (auth_method)
);

-- Table ns_organization stores definitions of organizations related to the operations.
-- At least one default organization must be configured.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_organization (
  organization_id          VARCHAR(256) PRIMARY KEY NOT NULL, -- ID of organization.
  display_name_key         VARCHAR(256),                      -- Localization key for the organization display name.
  is_default               BOOLEAN DEFAULT FALSE NOT NULL,    -- Flag indicating if this organization is the default.
  order_number             INTEGER NOT NULL,                  -- Ordering column for this organization, incrementing value, starts with 1.
  default_credential_name  VARCHAR(256),                      -- Default name of credential definition for authentication using Next Step, used by Web Flow.
  default_otp_name         VARCHAR(256)                       -- Default name of OTP definition for authentication using Next Step, used by Web Flow.
);

-- Table ns_step_definition stores definitions of authentication/authorization steps.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_step_definition (
  step_definition_id       INTEGER PRIMARY KEY NOT NULL,
  operation_name           VARCHAR(32) NOT NULL,
  operation_type           VARCHAR(32) NOT NULL,
  request_auth_method      VARCHAR(32),
  request_auth_step_result VARCHAR(32),
  response_priority        INTEGER NOT NULL,
  response_auth_method     VARCHAR(32),
  response_result          VARCHAR(32) NOT NULL,
  CONSTRAINT step_request_auth_method_fk FOREIGN KEY (request_auth_method) REFERENCES ns_auth_method (auth_method),
  CONSTRAINT step_response_auth_method_fk FOREIGN KEY (response_auth_method) REFERENCES ns_auth_method (auth_method)
);

-- Table ns_application stores Next Step applications.
CREATE TABLE ns_application (
  application_id         INTEGER PRIMARY KEY NOT NULL,                 -- Next Step application ID (autogenerated).
  name                   VARCHAR(256) NOT NULL,                        -- Application name used for identification.
  description            VARCHAR(256),                                 -- Description of the application.
  status                 VARCHAR(32) NOT NULL,                         -- Application status: ACTIVE, REMOVED.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,          -- Timestamp when application was created.
  timestamp_last_updated TIMESTAMP                                     -- Timestamp when application was last updated.
);

-- Table ns_credential_policy stores credential policies.
CREATE TABLE ns_credential_policy (
  credential_policy_id       INTEGER NOT NULL PRIMARY KEY,                 -- Credential policy ID (autogenerated).
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
  rotation_enabled           BOOLEAN DEFAULT FALSE NOT NULL,               -- Whether credential rotation is enabled.
  rotation_days              INTEGER,                                      -- Number of days for credential rotation.
  credential_temp_expiration INTEGER,                                      -- Expiration time of TEMPORARY credentials in seconds.
  username_gen_algorithm     VARCHAR(256) DEFAULT 'DEFAULT' NOT NULL,      -- Algorithm used for generating the username.
  username_gen_param         VARCHAR(4000) NOT NULL,                       -- Parameters used when generating the username.
  credential_gen_algorithm   VARCHAR(256) DEFAULT 'DEFAULT' NOT NULL,      -- Algorithm used for generating the credential.
  credential_gen_param       VARCHAR(4000) NOT NULL,                       -- Parameters used when generating the credential.
  credential_val_param       VARCHAR(4000) NOT NULL,                       -- Parameters used when validating the credential.
  timestamp_created          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,          -- Timestamp when policy was created.
  timestamp_last_updated     TIMESTAMP                                     -- Timestamp when policy was last updated.
);

-- Table ns_credential_policy stores one time password policies.
CREATE TABLE ns_otp_policy (
  otp_policy_id          INTEGER NOT NULL PRIMARY KEY,                -- One time password policy ID (autogenerated).
  name                   VARCHAR(256) NOT NULL,                       -- One time password policy name used for identification.
  description            VARCHAR(256),                                -- Description of the one time password policy.
  status                 VARCHAR(32) NOT NULL,                        -- One time password policy status: ACTIVE, REMOVED.
  length                 INTEGER NOT NULL,                            -- One time password length.
  attempt_limit          INTEGER,                                     -- Maximum number of authentication attempts.
  expiration_time        INTEGER,                                     -- One time password expiration time.
  gen_algorithm          VARCHAR(256) DEFAULT 'DEFAULT' NOT NULL,     -- Algorithm used for generating the one time password.
  gen_param              VARCHAR(4000) NOT NULL,                      -- Parameters used when generating the OTP.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when policy was created.
  timestamp_last_updated TIMESTAMP                                    -- Timestamp when policy was last updated.
);

-- Table ns_user_identity stores user identities.
CREATE TABLE ns_user_identity (
  user_id                VARCHAR(256) NOT NULL PRIMARY KEY,           -- User identity identifier (not autogenerated).
  status                 VARCHAR(32) NOT NULL,                        -- User identity status: ACTIVE, BLOCKED, REMOVED.
  extras                 TEXT,                                        -- Extra attributes with data related to user identity.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when user identity was created.
  timestamp_last_updated TIMESTAMP                                    -- Timestamp when user identity was last updated.
);

-- Table ns_user_contact stores contact information for user identities.
CREATE TABLE ns_user_contact (
  user_contact_id        INTEGER NOT NULL PRIMARY KEY,                -- User contact identifier (autogenerated).
  user_id                VARCHAR(256) NOT NULL,                       -- User identity identifier.
  name                   VARCHAR(256) NOT NULL,                       -- User contact name used for identification.
  type                   VARCHAR(32) NOT NULL,                        -- User contact type: PHONE, EMAIL, OTHER.
  value                  VARCHAR(256) NOT NULL,                       -- User contact value.
  is_primary             BOOLEAN DEFAULT FALSE NOT NULL,              -- Whether contact is primary.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when contact was created.
  timestamp_last_updated TIMESTAMP,                                   -- Timestamp when contact was last updated.
  CONSTRAINT ns_user_contact_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_user_identity stores history for user identities.
CREATE TABLE ns_user_identity_history (
  user_identity_history_id INTEGER NOT NULL PRIMARY KEY,                -- User identity history identifier (autogenerated).
  user_id                  VARCHAR(256) NOT NULL,                       -- User identity identifier.
  status                   VARCHAR(32) NOT NULL,                        -- User identity status: ACTIVE, BLOCKED, REMOVED.
  roles                    VARCHAR(256),                                -- Assigned user roles.
  extras                   TEXT,                                        -- Extra attributes with data related to user identity.
  timestamp_created        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when user identity snapshot was created.
  CONSTRAINT ns_user_identity_history_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_role stores user role definitions.
CREATE TABLE ns_role (
  role_id                INTEGER NOT NULL PRIMARY KEY,                -- Role identifier (autogenerated).
  name                   VARCHAR(256) NOT NULL,                       -- Role name used for identification.
  description            VARCHAR(256),                                -- Description of role.
  timestamp_created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when role was created.
  timestamp_last_updated TIMESTAMP                                    -- Timestamp when role was last updated.
);

-- Table ns_user_role stores assignment of roles to user identities.
CREATE TABLE ns_user_role (
  user_role_id             INTEGER NOT NULL PRIMARY KEY,                -- User role identifier (autogenerated).
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
  user_alias_id            INTEGER NOT NULL PRIMARY KEY,                -- User alias identifier (autogenerated).
  user_id                  VARCHAR(256) NOT NULL,                       -- User identity identifier.
  name                     VARCHAR(256) NOT NULL,                       -- User alias name used for identification.
  value                    VARCHAR(256) NOT NULL,                       -- User alias value.
  status                   VARCHAR(32) NOT NULL,                        -- User alias status: ACTIVE, REMOVED.
  extras                   TEXT,                                        -- Extra attributes with data related to user alias.
  timestamp_created        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when user alias was created.
  timestamp_last_updated   TIMESTAMP,                                   -- Timestamp when user alias was last updated.
  CONSTRAINT ns_user_alias_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_hashing_config stores configuration of hashing algorithms.
CREATE TABLE ns_hashing_config (
  hashing_config_id        INTEGER NOT NULL PRIMARY KEY,                -- Hashing configuration identifier (autogenerated).
  name                     VARCHAR(256) NOT NULL,                       -- Hashing configuration name used for identification.
  algorithm                VARCHAR(256) NOT NULL,                       -- Hashing algorithm name.
  status                   VARCHAR(32) NOT NULL,                        -- Hashing configuration status: ACTIVE, REMOVED.
  parameters               VARCHAR(256),                                -- Hashing algorithm parameters.
  timestamp_created        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when hashing configuration was created.
  timestamp_last_updated   TIMESTAMP                                    -- Timestamp when hashing configuration was last updated.
);

-- Table ns_credential_definition stores definitions of credentials with reference to credential policies and applications.
CREATE TABLE ns_credential_definition (
  credential_definition_id   INTEGER NOT NULL PRIMARY KEY,                -- Credential definition identifier (autogenerated).
  name                       VARCHAR(256) NOT NULL,                       -- Credential definition name used for identification.
  description                VARCHAR(256),                                -- Description of the credential definition.
  application_id             INTEGER NOT NULL,                            -- Application identifier.
  organization_id            VARCHAR(256),                                -- Organization this credential belongs to.
  credential_policy_id       INTEGER NOT NULL,                            -- Credential policy identifier.
  category                   VARCHAR(32) NOT NULL,                        -- Credential category: PASSWORD, PIN, OTHER.
  encryption_enabled         BOOLEAN DEFAULT FALSE NOT NULL,              -- Whether encryption of stored credentials is enabled.
  encryption_algorithm       VARCHAR(256),                                -- Algorithm used for stored credential encryption.
  hashing_enabled            BOOLEAN DEFAULT FALSE NOT NULL,              -- Whether credential hashing is enabled.
  hashing_config_id          INTEGER,                                     -- Algorithm used for credential hashing.
  e2e_encryption_enabled     BOOLEAN DEFAULT FALSE NOT NULL,              -- Whether end to end encryption of credential values is enabled.
  e2e_encryption_algorithm   VARCHAR(256),                                -- Algorithm used for end to end encryption of credential.
  e2e_encryption_transform   VARCHAR(256),                                -- Cipher transformation used for end to end encryption of credential.
  e2e_encryption_temporary   BOOLEAN DEFAULT FALSE NOT NULL,              -- Whether end to end encryption of temporary credential values is enabled.
  data_adapter_proxy_enabled BOOLEAN DEFAULT FALSE NOT NULL,             -- Whether credential API calls should be proxied through Data Adapter.
  status                     VARCHAR(32) NOT NULL,                        -- Credential definition status: ACTIVE, REMOVED.
  timestamp_created          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp when credential definition was created.
  timestamp_last_updated     TIMESTAMP,                                   -- Timestamp when credential definition was last updated.
  CONSTRAINT ns_credential_application_fk FOREIGN KEY (application_id) REFERENCES ns_application (application_id),
  CONSTRAINT ns_credential_policy_fk FOREIGN KEY (credential_policy_id) REFERENCES ns_credential_policy (credential_policy_id),
  CONSTRAINT ns_credential_hash_fk FOREIGN KEY (hashing_config_id) REFERENCES ns_hashing_config (hashing_config_id),
  CONSTRAINT ns_application_organization_fk FOREIGN KEY (organization_id) REFERENCES ns_organization (organization_id)
);

-- Table ns_otp_definition stores definitions of one time passwords with reference to credential policies and applications.
CREATE TABLE ns_otp_definition (
  otp_definition_id          INTEGER NOT NULL PRIMARY KEY,                -- One time password definition identifier (autogenerated).
  name                       VARCHAR(256) NOT NULL,                       -- One time password definition name used for identification.
  description                VARCHAR(256),                                -- Description of the one time password definition.
  application_id             INTEGER NOT NULL,                            -- Application identifier.
  otp_policy_id              INTEGER NOT NULL,                            -- One time password policy identifier.
  encryption_enabled         BOOLEAN DEFAULT FALSE NOT NULL,              -- Whether encryption of stored one time passwords is enabled.
  encryption_algorithm       VARCHAR(256),                                -- Algorithm used for stored one time password encryption.
  data_adapter_proxy_enabled BOOLEAN DEFAULT FALSE NOT NULL,              -- Whether one time password API calls should be proxied through Data Adapter.
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
  encryption_algorithm             VARCHAR(256),                        -- Encryption algorithm used for encrypting credential value.
  hashing_config_id                INTEGER,                             -- Hashing configuration used when credential value was hashed.
  timestamp_created                TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when credential was created.
  timestamp_expires                TIMESTAMP,                           -- Timestamp when credential expires.
  timestamp_blocked                TIMESTAMP,                           -- Timestamp when credential was blocked.
  timestamp_last_updated           TIMESTAMP,                           -- Timestamp when credential was last updated.
  timestamp_last_credential_change TIMESTAMP,                           -- Timestamp when credential value was last changed.
  timestamp_last_username_change   TIMESTAMP,                           -- Timestamp when username value was last changed.
  CONSTRAINT ns_credential_definition_fk FOREIGN KEY (credential_definition_id) REFERENCES ns_credential_definition (credential_definition_id),
  CONSTRAINT ns_credential_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_credential_history stores historical values of credentials.
CREATE TABLE ns_credential_history (
  credential_history_id       INTEGER NOT NULL PRIMARY KEY,                -- Credential history identifier (autogenerated).
  credential_definition_id    INTEGER NOT NULL,                            -- Credential identifier.
  user_id                     VARCHAR(256) NOT NULL,                       -- User identity identifier.
  user_name                   VARCHAR(256),                                -- Username.
  value                       VARCHAR(256) NOT NULL,                       -- Credential value.
  encryption_algorithm        VARCHAR(256),                                -- Encryption algorithm used for encrypting credential value.
  hashing_config_id           INTEGER,                                     -- Hashing configuration used when credential value was hashed.
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
  salt                        BYTEA,                       -- Cryptographic salt used when generating one time password.
  status                      VARCHAR(32) NOT NULL,                -- One time password status: ACTIVE, USED, BLOCKED, REMOVED.
  otp_data                    TEXT,                                -- Data used for generating one time password.
  attempt_counter             INTEGER DEFAULT 0 NOT NULL,          -- One time password attempt counter.
  failed_attempt_counter      INTEGER DEFAULT 0 NOT NULL,          -- One time password failed attempt counter.
  encryption_algorithm        VARCHAR(256),                        -- Encryption algorithm used for encrypting OTP value.
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
  operation_name                VARCHAR(32) NOT NULL,                -- Name of the operation, represents a type of the operation, for example, "login" or "authorize_payment".
  operation_data                TEXT NOT NULL,                       -- Signing data of the operation.
  operation_form_data           TEXT,                                -- Structured data of the operation that are displayed to the end user.
  application_id                VARCHAR(256),                        -- ID of the application that initiated the operation, usually OAuth 2.1 client ID.
  application_name              VARCHAR(256),                        -- Displayable name of the application that initiated the operation.
  application_description       VARCHAR(256),                        -- Displayable description of the application that initiated the operation.
  application_original_scopes   VARCHAR(256),                        -- Original OAuth 2.1 scopes used by the application that initiated the operation.
  application_extras            TEXT,                                -- Any additional information related to the application that initiated the operation.
  user_id                       VARCHAR(256),                        -- Associated user ID.
  organization_id               VARCHAR(256),                        -- Associated organization ID.
  user_account_status           VARCHAR(32),                         -- Status of the user account while initiated the operation - ACTIVE, NOT_ACTIVE.
  external_operation_name       VARCHAR(32),                         -- External operation name, which can further specify the operation purpose.
  external_transaction_id       VARCHAR(256),                        -- External transaction ID, for example ID of a payment in a transaction system.
  result                        VARCHAR(32),                         -- Operation result - CONTINUE, FAILED, DONE.
  timestamp_created             TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when this operation was created.
  timestamp_expires             TIMESTAMP,                           -- Timestamp of the expiration of the operation.
  CONSTRAINT ns_operation_organization_fk FOREIGN KEY (organization_id) REFERENCES ns_organization (organization_id),
  CONSTRAINT ns_operation_config_fk FOREIGN KEY (operation_name) REFERENCES ns_operation_config (operation_name)
);

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
  request_params              VARCHAR(4000),                       -- Additional request parameters.
  response_result             VARCHAR(32) NOT NULL,                -- Authentication step result: FAILED, CONTINUE, DONE.
  response_result_description VARCHAR(256),                        -- Additional information about the authentication step result.
  response_steps              VARCHAR(4000),                       -- Information about which methods are allowed in the next step.
  response_timestamp_created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when the record was created.
  response_timestamp_expires  TIMESTAMP,                           -- Timestamp when the operation step should expire.
  chosen_auth_method          VARCHAR(32),                         -- Information about which authentication method was chosen, in case user can chose the authentication method.
  mobile_token_active         BOOLEAN NOT NULL DEFAULT FALSE,      -- Information about if mobile token is active during the particular authentication step, in order to show the mobile token operation at the right time.
  authentication_id           VARCHAR(256),                        -- Reference to the authentication record.
  pa_operation_id             VARCHAR(256),                        -- PowerAuth operation ID for PowerAuth operations.
  pa_auth_context             VARCHAR(256),                        -- PowerAuth authentication context with additional details related to performed authentication.
  CONSTRAINT ns_history_pk PRIMARY KEY (operation_id, result_id),
  CONSTRAINT ns_history_operation_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id),
  CONSTRAINT ns_history_auth_method_fk FOREIGN KEY (request_auth_method) REFERENCES ns_auth_method (auth_method),
  CONSTRAINT ns_history_chosen_method_fk FOREIGN KEY (chosen_auth_method) REFERENCES ns_auth_method (auth_method),
  CONSTRAINT ns_history_authentication_fk FOREIGN KEY (authentication_id) REFERENCES ns_authentication (authentication_id)
);

-- Table ns_operation_afs stores AFS requests and responses.
CREATE TABLE ns_operation_afs (
  afs_action_id               INTEGER PRIMARY KEY NOT NULL,                -- ID of the AFS action.
  operation_id                VARCHAR(256) NOT NULL,                       -- Operation ID.
  request_afs_action          VARCHAR(256) NOT NULL,                       -- Information about requested AFS action.
  request_step_index          INTEGER NOT NULL,                            -- Counter within the specific operation step that is associated with AFS action, e.g. to differentiate multiple authentication attempts. Incrementing value, starts with 1.
  request_afs_extras          VARCHAR(256),                                -- Additional information about AFS action, typically a cookie values used in AFS system.
  response_afs_apply          BOOLEAN NOT NULL DEFAULT FALSE,              -- Response information about if AFS was applied.
  response_afs_label          VARCHAR(256),                                -- Response AFS label (information about what should the application do).
  response_afs_extras         VARCHAR(256),                                -- Additional information sent in AFS response.
  timestamp_created           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp this AFS action was created.
  CONSTRAINT operation_afs_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id)
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
);

-- Table da_sms_authorization stores data for SMS OTP authorization.
CREATE TABLE da_sms_authorization (
  message_id           VARCHAR(256) PRIMARY KEY NOT NULL,   -- SMS message ID, ID of SMS OTP.
  operation_id         VARCHAR(256) NOT NULL,               -- Operation ID.
  user_id              VARCHAR(256) NOT NULL,               -- User ID.
  organization_id      VARCHAR(256),                        -- Organization ID.
  operation_name       VARCHAR(32) NOT NULL,                -- Name of the operation that triggered the SMS (login, authorize_payment, ...).
  authorization_code   VARCHAR(32) NOT NULL,                -- Value of the authorization code sent in the SMS.
  salt                 BYTEA NOT NULL,                      -- Salt used for authorization code calculation.
  message_text         TEXT NOT NULL,                       -- Full SMS message text.
  verify_request_count INTEGER,                             -- Number of verification attempts.
  verified             BOOLEAN DEFAULT FALSE,               -- Flag indicating if this SMS OTP was successfully verified.
  timestamp_created    TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp when the SMS OTP was generated.
  timestamp_verified   TIMESTAMP,                           -- Timestamp when the SMS OTP was successfully validated.
  timestamp_expires    TIMESTAMP                            -- Timestamp when the SMS OTP expires.
);

-- Table da_user_credentials stores built-in users for the data adapter
CREATE TABLE da_user_credentials (
  user_id               VARCHAR(128) PRIMARY KEY NOT NULL,  -- User ID. Technical identifier of the user.
  username              VARCHAR(256) NOT NULL,              -- Username, the displayable value that users use to sign in.
  password_hash         VARCHAR(256) NOT NULL,              -- Bcrypt hash of the password.
  family_name           VARCHAR(256) NOT NULL,              -- User family name.
  given_name            VARCHAR(256) NOT NULL,              -- User given name.
  organization_id       VARCHAR(64)  NOT NULL,              -- User organization ID.
  phone_number          VARCHAR(256) NOT NULL               -- Full phone number, should be stored in format that allows easy SMS message sending.
);

-- Table for the list of consent currently given by a user
CREATE TABLE tpp_consent (
  consent_id            VARCHAR(64) PRIMARY KEY NOT NULL,   -- Consent ID.
  consent_name          VARCHAR(128) NOT NULL,              -- Consent name, localization key or full displayable value.
  consent_text          TEXT NOT NULL,                      -- Consent text, localization key or full displayable value with optional placeholders.
  version               INT NOT NULL                        -- Consent version.
);

-- Table for the list of changes in consent history by given user
CREATE TABLE tpp_user_consent (
  id                    INTEGER PRIMARY KEY NOT NULL,                -- User given consent ID.
  user_id               VARCHAR(256) NOT NULL,                       -- User ID.
  client_id             VARCHAR(256) NOT NULL,                       -- OAuth 2.1 client ID.
  consent_id            VARCHAR(64) NOT NULL,                        -- Consent ID.
  external_id           VARCHAR(256),                                -- External ID associated with the consent approval, usually the operation ID.
  consent_parameters    TEXT NOT NULL,                               -- Specific parameters that were filled in into the user consent template.
  timestamp_created     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- Timestamp the consent with given ID was first created.
  timestamp_updated     TIMESTAMP                                    -- Timestamp the consent with given ID was given again before it was revoked (updated, prolonged).
);

CREATE TABLE tpp_user_consent_history (
  id                    INTEGER PRIMARY KEY NOT NULL,                -- ID of the consent history record.
  user_id               VARCHAR(256) NOT NULL,                       -- User ID.
  client_id             VARCHAR(256) NOT NULL,                       -- Client ID.
  consent_id            VARCHAR(64) NOT NULL,                        -- Consent ID.
  consent_change        VARCHAR(16) NOT NULL,                        -- Type of the consent change: APPROVE, PROLONG, REJECT
  external_id           VARCHAR(256),                                -- External ID that was responsible for this specific consent change, usually the operation ID.
  consent_parameters    TEXT NOT NULL,                               -- Specific parameters that were filled in into the user consent template in this consent change.
  timestamp_created     TIMESTAMP DEFAULT CURRENT_TIMESTAMP          -- Timestamp of the consent change.
);

CREATE TABLE tpp_detail (
  tpp_id                INTEGER PRIMARY KEY NOT NULL,                -- ID of the TPP provider.
  tpp_name              VARCHAR(256) NOT NULL,                       -- Name of the TPP provider.
  tpp_license           VARCHAR(256) NOT NULL,                       -- Information about the TPP license.
  tpp_info              TEXT NULL,                                   -- Additional information about the TPP provider, if available.
  tpp_address           TEXT NULL,                                   -- TPP address, if available.
  tpp_website           TEXT NULL,                                   -- TPP website, if available.
  tpp_phone             VARCHAR(256) NULL,                           -- TPP phone number, if available.
  tpp_email             VARCHAR(256) NULL,                           -- TPP e-mail, if available.
  tpp_logo              TEXT NULL,                                   -- TPP logo, if available.
  tpp_blocked           BOOLEAN DEFAULT FALSE NOT NULL               -- Indication if this TPP provider is blocked or not.
);

CREATE TABLE tpp_app_detail (
  tpp_id                INTEGER NOT NULL,                            -- TPP ID.
  app_client_id         VARCHAR(256) NOT NULL,                       -- TPP app ID, represented as OAuth 2.1 client ID and connecting the application to OAuth 2.1 credentials.
  app_name              VARCHAR(256) NOT NULL,                       -- TPP app name.
  app_info              TEXT NULL,                                   -- An arbitrary additional info about TPP app, if available.
  app_type              VARCHAR(32) NULL,                            -- Application type, "web" or "native".
  CONSTRAINT tpp_detail_pk PRIMARY KEY (tpp_id, app_client_id),
  CONSTRAINT tpp_detail_fk FOREIGN KEY (tpp_id) REFERENCES tpp_detail (tpp_id),
  CONSTRAINT tpp_client_secret_fk FOREIGN KEY (app_client_id) REFERENCES oauth2_registered_client (client_id)
);

-- Table audit_log stores auditing information
CREATE TABLE IF NOT EXISTS audit_log (
    audit_log_id       VARCHAR(36) PRIMARY KEY,
    application_name   VARCHAR(256) NOT NULL,
    audit_level        VARCHAR(32) NOT NULL,
    audit_type         VARCHAR(256),
    timestamp_created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message            TEXT NOT NULL,
    exception_message  TEXT,
    stack_trace        TEXT,
    param              TEXT,
    calling_class      VARCHAR(256) NOT NULL,
    thread_name        VARCHAR(256) NOT NULL,
    version            VARCHAR(256),
    build_time         TIMESTAMP
);

-- Table audit_param stores auditing parameters
CREATE TABLE IF NOT EXISTS audit_param (
    audit_log_id       VARCHAR(36),
    timestamp_created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    param_key          VARCHAR(256),
    param_value        VARCHAR(4000)
);

CREATE INDEX wf_operation_hash ON wf_operation_session (operation_hash);
CREATE INDEX wf_websocket_session ON wf_operation_session (websocket_session_id);
CREATE INDEX ns_operation_pending ON ns_operation (user_id, result);
CREATE UNIQUE INDEX ns_operation_afs_unique ON ns_operation_afs (operation_id, request_afs_action, request_step_index);
CREATE UNIQUE INDEX ns_application_name ON ns_application (name);
CREATE UNIQUE INDEX ns_credential_policy_name ON ns_credential_policy (name);
CREATE UNIQUE INDEX ns_otp_policy_name ON ns_otp_policy (name);
CREATE INDEX ns_user_contact_user_id ON ns_user_contact (user_id);
CREATE UNIQUE INDEX ns_user_contact_unique ON ns_user_contact (user_id, name, type);
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
CREATE INDEX ns_otp_storage_user_id_status ON ns_otp_storage (user_id, status);
CREATE INDEX ns_otp_storage_operation_id ON ns_otp_storage (operation_id);
CREATE INDEX ns_authentication_user_id ON ns_authentication (user_id);
CREATE INDEX ns_authentication_operation_id ON ns_authentication (operation_id);
CREATE INDEX ns_authentication_timestamp_created ON ns_authentication (timestamp_created);
CREATE UNIQUE INDEX ns_hashing_config_name ON ns_hashing_config (name);
CREATE UNIQUE INDEX ns_user_alias_unique ON ns_user_alias (user_id, name);
CREATE UNIQUE INDEX ns_user_role_unique ON ns_user_role (user_id, role_id);
CREATE INDEX IF NOT EXISTS audit_log_timestamp ON audit_log (timestamp_created);
CREATE INDEX IF NOT EXISTS audit_log_application ON audit_log (application_name);
CREATE INDEX IF NOT EXISTS audit_log_level ON audit_log (audit_level);
CREATE INDEX IF NOT EXISTS audit_log_type ON audit_log (audit_type);
CREATE INDEX IF NOT EXISTS audit_param_log ON audit_param (audit_log_id);
CREATE INDEX IF NOT EXISTS audit_param_timestamp ON audit_param (timestamp_created);
CREATE INDEX IF NOT EXISTS audit_param_key ON audit_param (param_key);
CREATE INDEX IF NOT EXISTS audit_param_value ON audit_param (param_value);

-- Foreign keys for user identity, to be used only when all user identities are stored in Next Step
-- ALTER TABLE ns_operation ADD CONSTRAINT ns_operation_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id);
-- ALTER TABLE ns_user_prefs ADD CONSTRAINT ns_user_prefs_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id);
-- ALTER TABLE ns_otp_storage ADD CONSTRAINT ns_otp_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id);
-- ALTER TABLE ns_authentication ADD CONSTRAINT ns_auth_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id);

CREATE TABLE shedlock (name VARCHAR(64) NOT NULL, lock_until TIMESTAMP WITHOUT TIME ZONE NOT NULL, locked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL, locked_by VARCHAR(255) NOT NULL, CONSTRAINT shedlock_pkey PRIMARY KEY (name));
