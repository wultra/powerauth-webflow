--
--  Create sequences.
--
CREATE SEQUENCE tpp_detail_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE tpp_user_consent_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE tpp_user_consent_history_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_operation_afs_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_application_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_credential_policy_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_otp_policy_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_user_contact_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_user_identity_history_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_role_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_user_role_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_user_alias_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_hashing_config_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_credential_definition_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_otp_definition_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;
CREATE SEQUENCE ns_credential_history_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE;

-- Table oauth2_authorization stores information about OAuth 2.1 authorizations
-- Source: https://github.com/spring-projects/spring-authorization-server/blob/main/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql
CREATE TABLE oauth2_authorization (
    id VARCHAR(100) NOT NULL,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000) DEFAULT NULL,
    attributes CLOB DEFAULT NULL,
    state VARCHAR(500) DEFAULT NULL,
    authorization_code_value CLOB DEFAULT NULL,
    authorization_code_issued_at TIMESTAMP DEFAULT NULL,
    authorization_code_expires_at TIMESTAMP DEFAULT NULL,
    authorization_code_metadata CLOB DEFAULT NULL,
    access_token_value CLOB DEFAULT NULL,
    access_token_issued_at TIMESTAMP DEFAULT NULL,
    access_token_expires_at TIMESTAMP DEFAULT NULL,
    access_token_metadata CLOB DEFAULT NULL,
    access_token_type VARCHAR(100) DEFAULT NULL,
    access_token_scopes VARCHAR(1000) DEFAULT NULL,
    oidc_id_token_value CLOB DEFAULT NULL,
    oidc_id_token_issued_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_expires_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_metadata CLOB DEFAULT NULL,
    refresh_token_value CLOB DEFAULT NULL,
    refresh_token_issued_at TIMESTAMP DEFAULT NULL,
    refresh_token_expires_at TIMESTAMP DEFAULT NULL,
    refresh_token_metadata CLOB DEFAULT NULL,
    user_code_value CLOB DEFAULT NULL,
    user_code_issued_at TIMESTAMP DEFAULT NULL,
    user_code_expires_at TIMESTAMP DEFAULT NULL,
    user_code_metadata CLOB DEFAULT NULL,
    device_code_value CLOB DEFAULT NULL,
    device_code_issued_at TIMESTAMP DEFAULT NULL,
    device_code_expires_at TIMESTAMP DEFAULT NULL,
    device_code_metadata CLOB DEFAULT NULL,
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
  operation_id              VARCHAR2(256 CHAR) PRIMARY KEY NOT NULL,      -- Operation ID.
  http_session_id           VARCHAR2(256 CHAR) NOT NULL,                  -- HTTP session ID related to given operation.
  operation_hash            VARCHAR2(256 CHAR),                           -- Hash of the operation ID.
  websocket_session_id      VARCHAR2(32 CHAR),                            -- WebSocket Session ID.
  client_ip_address         VARCHAR2(32 CHAR),                            -- Client IP address, if available.
  result                    VARCHAR2(32 CHAR) NOT NULL,                   -- Result of the operation, stored in the session.
  timestamp_created         TIMESTAMP                                     -- Timestamp of the record creation.
);

-- Table wf_afs_config is used to configure anti-fraud system parameters.
CREATE TABLE wf_afs_config (
  config_id                 VARCHAR2(256 CHAR) PRIMARY KEY NOT NULL,      -- AFS config ID.
  js_snippet_url            VARCHAR2(256 CHAR) NOT NULL,                  -- URL of the AFS JavaScript snippet (relative to application or absolute).
  parameters                CLOB                                          -- Additional AFS snippet parameters.
);

-- Table wf_certificate_verification is used for storing information about verified client TLS certificates.
CREATE TABLE wf_certificate_verification (
  operation_id               VARCHAR2(256 CHAR) NOT NULL,                 -- Operation ID associated with the certificate verification.
  auth_method                VARCHAR2(32 CHAR) NOT NULL,                  -- Authentication method in which the certificate authentication was used.
  client_certificate_issuer  VARCHAR2(4000 CHAR) NOT NULL,                -- Certificate attribute representing the certificate issuer.
  client_certificate_subject VARCHAR2(4000 CHAR) NOT NULL,                -- Certificate attribute representing the certificate subject.
  client_certificate_sn      VARCHAR2(256 CHAR) NOT NULL,                 -- Certificate attribute representing the certificate serial number.
  operation_data             CLOB NOT NULL,                               -- Operation data that were included in the certificate authentication request.
  timestamp_verified         TIMESTAMP NOT NULL,                          -- Timestamp of the certificate verification.
  CONSTRAINT wf_certificate_verification_pk PRIMARY KEY (operation_id, auth_method)
);

-- Table ns_auth_method stores configuration of authentication methods.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_auth_method (
  auth_method        VARCHAR2(32 CHAR) PRIMARY KEY NOT NULL,  -- Name of the authentication method: APPROVAL_SCA, CONSENT, INIT, LOGIN_SCA, POWERAUTH_TOKEN, SHOW_OPERATION_DETAIL, SMS_KEY, USER_ID_ASSIGN, USERNAME_PASSWORD_AUTH, OTP_CODE.
  order_number       INTEGER NOT NULL,                        -- Order of the authentication method, incrementing value, starts with 1.
  check_user_prefs   NUMBER(1) DEFAULT 0 NOT NULL,            -- Indication if the authentication method requires checking the user preference first.
  user_prefs_column  INTEGER,                                 -- In case the previous column is 'true', this is pointer to the user preferences configuration column index.
  user_prefs_default NUMBER(1) DEFAULT 0,                     -- Default value of the user preferences, in case the per-user preference is not found.
  check_auth_fails   NUMBER(1) DEFAULT 0 NOT NULL,            -- Indication if the methods can fail, and hence the fail count must be checked.
  max_auth_fails     INTEGER,                                 -- Maximum allowed number of authentication fails.
  has_user_interface NUMBER(1) DEFAULT 0,                     -- Indication of if the given method has any user interface in the web flow.
  has_mobile_token   NUMBER(1) DEFAULT 0,                     -- Indication of if the given authentication method has mobile token as a part of the flow.
  display_name_key   VARCHAR2(32 CHAR)                        -- Localization key to the display name of the authentication method.
);

-- Table ns_operation_config stores configuration of operations.
-- Each operation type (defined by operation_name) has a related mobile token template and configuration of signatures.
CREATE TABLE ns_operation_config (
  operation_name            VARCHAR2(32 CHAR) PRIMARY KEY NOT NULL,   -- Name of the operation, for example "login" or "authorize_payment".
  template_version          VARCHAR2(1 CHAR) NOT NULL,                -- Version of the template, used for data signing base.
  template_id               INTEGER NOT NULL,                         -- ID of the template, used for data signing base.
  mobile_token_enabled      NUMBER(1) DEFAULT 0 NOT NULL,             -- Flag indicating if the mobile token is enabled for this operation type.
  mobile_token_mode         VARCHAR2(256 CHAR) NOT NULL,              -- Configuration of mobile token for this operation, for example, if 1FA or 2FA is supported, and which 2FA variants. The field contains a serialized JSON with configuration.
  afs_enabled               NUMBER(1) DEFAULT 0 NOT NULL,             -- Flag indicating if AFS system is enabled.
  afs_config_id             VARCHAR2(256 CHAR),                       -- Configuration of AFS system.
  expiration_time           INTEGER,                                  -- Expiration time in seconds, which overrides global Next Step configuration.
  CONSTRAINT ns_operation_config_afs_fk FOREIGN KEY (afs_config_id) REFERENCES wf_afs_config (config_id)
);

-- Table ns_operation_method_config stores configuration of authentication methods per operation name.
CREATE TABLE ns_operation_method_config (
  operation_name     VARCHAR2(32 CHAR) NOT NULL,             -- Name of the operation, for example "login" or "authorize_payment".
  auth_method        VARCHAR2(32 CHAR) NOT NULL,             -- Name of the authentication method: APPROVAL_SCA, CONSENT, INIT, LOGIN_SCA, POWERAUTH_TOKEN, SHOW_OPERATION_DETAIL, SMS_KEY, USER_ID_ASSIGN, USERNAME_PASSWORD_AUTH, OTP_CODE.
  max_auth_fails     INTEGER NOT NULL,                       -- Maximum allowed number of authentication fails.
  PRIMARY KEY (operation_name, auth_method),
  CONSTRAINT ns_operation_method_fk1 FOREIGN KEY (operation_name) REFERENCES ns_operation_config (operation_name),
  CONSTRAINT ns_operation_method_fk2 FOREIGN KEY (auth_method) REFERENCES ns_auth_method (auth_method)
);

-- Table ns_organization stores definitions of organizations related to the operations.
-- At least one default organization must be configured.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_organization (
  organization_id          VARCHAR2(256 CHAR) PRIMARY KEY NOT NULL,   -- ID of organization.
  display_name_key         VARCHAR2(256 CHAR),                        -- Localization key for the organization display name.
  is_default               NUMBER(1) DEFAULT 0 NOT NULL,              -- Flag indicating if this organization is the default.
  order_number             INTEGER NOT NULL,                          -- Ordering column for this organization, incrementing value, starts with 1.
  default_credential_name  VARCHAR2(256 CHAR),                        -- Default name of credential definition for authentication using Next Step.
  default_otp_name         VARCHAR2(256 CHAR)                         -- Default name of OTP definition for authentication using Next Step.
);

-- Table ns_step_definition stores definitions of authentication/authorization steps.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_step_definition (
  step_definition_id       INTEGER PRIMARY KEY NOT NULL,                  -- Step definition ID.
  operation_name           VARCHAR2(32 CHAR) NOT NULL,                    -- Operation name for which this step definition is valid.
  operation_type           VARCHAR2(32 CHAR) NOT NULL,                    -- Type of the operation change: CREATE or UPDATE
  request_auth_method      VARCHAR2(32 CHAR),                             -- Operation authentication method that was selected by the user or developer.
  request_auth_step_result VARCHAR2(32 CHAR),                             -- Result of the authentication method execution: CONFIRMED, CANCELED, AUTH_METHOD_FAILED, AUTH_FAILED
  response_priority        INTEGER NOT NULL,                              -- Response priority (ordering column).
  response_auth_method     VARCHAR2(32 CHAR),                             -- Response with the authentication method that should be applied next.
  response_result          VARCHAR2(32 CHAR) NOT NULL,                    -- Result of the operation: CONTINUE, FAILED, or DONE
  CONSTRAINT ns_step_request_auth_method_fk FOREIGN KEY (request_auth_method) REFERENCES ns_auth_method (auth_method),
  CONSTRAINT ns_step_response_auth_method_fk FOREIGN KEY (response_auth_method) REFERENCES ns_auth_method (auth_method)
);

-- Table ns_application stores Next Step applications.
CREATE TABLE ns_application (
  application_id         NUMBER(19,0) NOT NULL PRIMARY KEY,       -- Next Step application ID (autogenerated).
  name                   VARCHAR2(256 CHAR) NOT NULL,             -- Application name used for identification.
  description            VARCHAR2(256 CHAR),                      -- Description of the application.
  status                 VARCHAR2(32 CHAR) NOT NULL,              -- Application status: ACTIVE, REMOVED.
  timestamp_created      TIMESTAMP,                               -- Timestamp when application was created.
  timestamp_last_updated TIMESTAMP                                -- Timestamp when application was last updated.
);

-- Table ns_credential_policy stores credential policies.
CREATE TABLE ns_credential_policy (
  credential_policy_id       NUMBER(19,0) NOT NULL PRIMARY KEY,             -- Credential policy ID (autogenerated).
  name                       VARCHAR2(256 CHAR) NOT NULL,                   -- Credential policy name used for identification.
  description                VARCHAR2(256 CHAR),                            -- Description of the credential policy.
  status                     VARCHAR2(32 CHAR) NOT NULL,                    -- Credential policy status: ACTIVE, REMOVED.
  username_length_min        NUMBER(10,0),                                  -- Minimum length of username.
  username_length_max        NUMBER(10,0),                                  -- Maximum length of username.
  username_allowed_pattern   VARCHAR2(256 CHAR),                            -- Allowed pattern for username (regular expression).
  credential_length_min      NUMBER(10,0),                                  -- Minimum length of credential value.
  credential_length_max      NUMBER(10,0),                                  -- Maximum length of credential value.
  limit_soft                 NUMBER(10,0),                                  -- Soft limit of failed attempts.
  limit_hard                 NUMBER(10,0),                                  -- Hard limit of failed attempts.
  check_history_count        NUMBER(10,0) DEFAULT 0 NOT NULL,               -- Number of historical credential values to check.
  rotation_enabled           NUMBER(1) DEFAULT 0 NOT NULL,                  -- Whether credential rotation is enabled.
  rotation_days              NUMBER(10,0),                                  -- Number of days for credential rotation.
  credential_temp_expiration INTEGER,                                       -- Expiration time of TEMPORARY credentials in seconds.
  username_gen_algorithm     VARCHAR2(256 CHAR) DEFAULT 'DEFAULT' NOT NULL, -- Algorithm used for generating the username.
  username_gen_param         VARCHAR2(4000 CHAR) NOT NULL,                  -- Parameters used when generating the username.
  credential_gen_algorithm   VARCHAR2(256 CHAR) DEFAULT 'DEFAULT' NOT NULL, -- Algorithm used for generating the credential.
  credential_gen_param       VARCHAR2(4000 CHAR) NOT NULL,                  -- Parameters used when generating the credential.
  credential_val_param       VARCHAR2(4000 CHAR) NOT NULL,                  -- Parameters used when validating the credential.
  timestamp_created          TIMESTAMP,                                     -- Timestamp when policy was created.
  timestamp_last_updated     TIMESTAMP                                      -- Timestamp when policy was last updated.
);

-- Table ns_credential_policy stores one time password policies.
CREATE TABLE ns_otp_policy (
  otp_policy_id          NUMBER(19,0) NOT NULL PRIMARY KEY,                 -- One time password policy ID (autogenerated).
  name                   VARCHAR2(256 CHAR) NOT NULL,                       -- One time password policy name used for identification.
  description            VARCHAR2(256 CHAR),                                -- Description of the one time password policy.
  status                 VARCHAR2(32 CHAR) NOT NULL,                        -- One time password policy status: ACTIVE, REMOVED.
  length                 NUMBER(10,0) NOT NULL,                             -- One time password length.
  attempt_limit          NUMBER(10,0),                                      -- Maximum number of authentication attempts.
  expiration_time        NUMBER(10,0),                                      -- One time password expiration time.
  gen_algorithm          VARCHAR2(256 CHAR) DEFAULT 'DEFAULT' NOT NULL,     -- Algorithm used for generating the one time password.
  gen_param              VARCHAR2(4000 CHAR) NOT NULL,                      -- Parameters used when generating the OTP.
  timestamp_created      TIMESTAMP,                                         -- Timestamp when policy was created.
  timestamp_last_updated TIMESTAMP                                          -- Timestamp when policy was last updated.
);

-- Table ns_user_identity stores user identities.
CREATE TABLE ns_user_identity (
  user_id                VARCHAR2(256 CHAR) NOT NULL PRIMARY KEY,           -- User identity identifier (not autogenerated).
  status                 VARCHAR2(32 CHAR) NOT NULL,                        -- User identity status: ACTIVE, BLOCKED, REMOVED.
  extras                 CLOB,                                              -- Extra attributes with data related to user identity.
  timestamp_created      TIMESTAMP,                                         -- Timestamp when user identity was created.
  timestamp_last_updated TIMESTAMP                                          -- Timestamp when user identity was last updated.
);

-- Table ns_user_contact stores contact information for user identities.
CREATE TABLE ns_user_contact (
  user_contact_id        NUMBER(19,0) NOT NULL PRIMARY KEY,                 -- User contact identifier (autogenerated).
  user_id                VARCHAR2(256 CHAR) NOT NULL,                       -- User identity identifier.
  name                   VARCHAR2(256 CHAR) NOT NULL,                       -- User contact name used for identification.
  type                   VARCHAR2(32 CHAR) NOT NULL,                        -- User contact type: PHONE, EMAIL, OTHER.
  value                  VARCHAR2(256 CHAR) NOT NULL,                       -- User contact value.
  is_primary             NUMBER(1) DEFAULT 0 NOT NULL,                      -- Whether contact is primary.
  timestamp_created      TIMESTAMP,                                         -- Timestamp when contact was created.
  timestamp_last_updated TIMESTAMP,                                         -- Timestamp when contact was last updated.
  CONSTRAINT ns_user_contact_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_user_identity stores history for user identities.
CREATE TABLE ns_user_identity_history (
  user_identity_history_id NUMBER(19,0) NOT NULL PRIMARY KEY,               -- User identity history identifier (autogenerated).
  user_id                  VARCHAR2(256 CHAR) NOT NULL,                     -- User identity identifier.
  status                   VARCHAR2(32 CHAR) NOT NULL,                      -- User identity status: ACTIVE, BLOCKED, REMOVED.
  roles                    VARCHAR2(256 CHAR),                              -- Assigned user roles.
  extras                   CLOB,                                            -- Extra attributes with data related to user identity.
  timestamp_created        TIMESTAMP,                                       -- Timestamp when user identity snapshot was created.
  CONSTRAINT ns_user_identity_history_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_role stores user role definitions.
CREATE TABLE ns_role (
  role_id                NUMBER(19,0) NOT NULL PRIMARY KEY,                 -- Role identifier (autogenerated).
  name                   VARCHAR2(256 CHAR) NOT NULL,                       -- Role name used for identification.
  description            VARCHAR2(256 CHAR),                                -- Description of role.
  timestamp_created      TIMESTAMP,                                         -- Timestamp when role was created.
  timestamp_last_updated TIMESTAMP                                          -- Timestamp when role was last updated.
);

-- Table ns_user_role stores assignment of roles to user identities.
CREATE TABLE ns_user_role (
  user_role_id             NUMBER(19,0) NOT NULL PRIMARY KEY,               -- User role identifier (autogenerated).
  user_id                  VARCHAR2(256 CHAR) NOT NULL,                     -- User identity identifier.
  role_id                  NUMBER(19,0) NOT NULL,                           -- Role identifier.
  status                   VARCHAR2(32 CHAR) NOT NULL,                      -- User role status: ACTIVE, REMOVED.
  timestamp_created        TIMESTAMP,                                       -- Timestamp when user role was created.
  timestamp_last_updated   TIMESTAMP,                                       -- Timestamp when user role was last updated.
  CONSTRAINT ns_role_identity_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id),
  CONSTRAINT ns_user_role_fk FOREIGN KEY (role_id) REFERENCES ns_role (role_id)
);

-- Table ns_user_alias stores user aliases.
CREATE TABLE ns_user_alias (
  user_alias_id            NUMBER(19,0) NOT NULL PRIMARY KEY,               -- User alias identifier (autogenerated).
  user_id                  VARCHAR2(256 CHAR) NOT NULL,                     -- User identity identifier.
  name                     VARCHAR2(256 CHAR) NOT NULL,                     -- User alias name used for identification.
  value                    VARCHAR2(256 CHAR) NOT NULL,                     -- User alias value.
  status                   VARCHAR2(32 CHAR) NOT NULL,                      -- User alias status: ACTIVE, REMOVED.
  extras                   CLOB,                                            -- Extra attributes with data related to user alias.
  timestamp_created        TIMESTAMP,                                       -- Timestamp when user alias was created.
  timestamp_last_updated   TIMESTAMP,                                       -- Timestamp when user alias was last updated.
  CONSTRAINT ns_user_alias_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_hashing_config stores configuration of hashing algorithms.
CREATE TABLE ns_hashing_config (
  hashing_config_id        NUMBER(19,0) NOT NULL PRIMARY KEY,               -- Hashing configuration identifier (autogenerated).
  name                     VARCHAR2(256 CHAR) NOT NULL,                     -- Hashing configuration name used for identification.
  algorithm                VARCHAR2(256 CHAR) NOT NULL,                     -- Hashing algorithm name.
  status                   VARCHAR2(32 CHAR) NOT NULL,                      -- Hashing configuration status: ACTIVE, REMOVED.
  parameters               VARCHAR2(256 CHAR),                              -- Hashing algorithm parameters.
  timestamp_created        TIMESTAMP,                                       -- Timestamp when hashing configuration was created.
  timestamp_last_updated   TIMESTAMP                                        -- Timestamp when hashing configuration was last updated.
);

-- Table ns_credential_definition stores definitions of credentials with reference to credential policies and applications.
CREATE TABLE ns_credential_definition (
  credential_definition_id   NUMBER(19,0) NOT NULL PRIMARY KEY,               -- Credential definition identifier (autogenerated).
  name                       VARCHAR2(256 CHAR) NOT NULL,                     -- Credential definition name used for identification.
  description                VARCHAR2(256 CHAR),                              -- Description of the credential definition.
  application_id             NUMBER(19,0) NOT NULL,                           -- Application identifier.
  organization_id            VARCHAR2(256 CHAR),                              -- Organization this credential belongs to.
  credential_policy_id       NUMBER(19,0) NOT NULL,                           -- Credential policy identifier.
  category                   VARCHAR2(32 CHAR) NOT NULL,                      -- Credential category: PASSWORD, PIN, OTHER.
  encryption_enabled         NUMBER(1) DEFAULT 0 NOT NULL,                    -- Whether encryption of stored credentials is enabled.
  encryption_algorithm       VARCHAR2(256 CHAR),                              -- Algorithm used for stored credential encryption.
  hashing_enabled            NUMBER(1) DEFAULT 0 NOT NULL,                    -- Whether credential hashing is enabled.
  hashing_config_id          NUMBER(19,0),                                    -- Algorithm used for credential hashing.
  e2e_encryption_enabled     NUMBER(1) DEFAULT 0 NOT NULL,                    -- Whether end to end encryption of credential values is enabled.
  e2e_encryption_algorithm   VARCHAR2(256 CHAR),                              -- Algorithm used for end to end encryption of credential.
  e2e_encryption_transform   VARCHAR2(256 CHAR),                              -- Cipher transformation used for end to end encryption of credential.
  e2e_encryption_temporary   NUMBER(1) DEFAULT 0 NOT NULL,                    -- Whether end to end encryption of temporary credential values is enabled.
  data_adapter_proxy_enabled NUMBER(1) DEFAULT 0 NOT NULL,                    -- Whether credential API calls should be proxied through Data Adapter.
  status                     VARCHAR2(32 CHAR) NOT NULL,                      -- Credential definition status: ACTIVE, REMOVED.
  timestamp_created          TIMESTAMP,                                       -- Timestamp when credential definition was created.
  timestamp_last_updated     TIMESTAMP,                                       -- Timestamp when credential definition was last updated.
  CONSTRAINT ns_credential_application_fk FOREIGN KEY (application_id) REFERENCES ns_application (application_id),
  CONSTRAINT ns_credential_policy_fk FOREIGN KEY (credential_policy_id) REFERENCES ns_credential_policy (credential_policy_id),
  CONSTRAINT ns_credential_hash_fk FOREIGN KEY (hashing_config_id) REFERENCES ns_hashing_config (hashing_config_id),
  CONSTRAINT ns_application_organization_fk FOREIGN KEY (organization_id) REFERENCES ns_organization (organization_id)
);

-- Table ns_otp_definition stores definitions of one time passwords with reference to credential policies and applications.
CREATE TABLE ns_otp_definition (
  otp_definition_id          NUMBER(19,0) NOT NULL PRIMARY KEY,               -- One time password definition identifier (autogenerated).
  name                       VARCHAR2(256 CHAR) NOT NULL,                     -- One time password definition name used for identification.
  description                VARCHAR2(256 CHAR),                              -- Description of the one time password definition.
  application_id             NUMBER(19,0) NOT NULL,                           -- Application identifier.
  otp_policy_id              NUMBER(19,0) NOT NULL,                           -- One time password policy identifier.
  encryption_enabled         NUMBER(1) DEFAULT 0 NOT NULL,                    -- Whether encryption of stored one time passwords is enabled.
  encryption_algorithm       VARCHAR2(256 CHAR),                              -- Algorithm used for stored one time password encryption.
  data_adapter_proxy_enabled NUMBER(1) DEFAULT 0 NOT NULL,                    -- Whether one time password API calls should be proxied through Data Adapter.
  status                     VARCHAR2(32 CHAR) NOT NULL,                      -- One time password definition status: ACTIVE, REMOVED.
  timestamp_created          TIMESTAMP,                                       -- Timestamp when one time password definition was created.
  timestamp_last_updated     TIMESTAMP,                                       -- Timestamp when one time password definition was last updated.
  CONSTRAINT ns_otp_application_fk FOREIGN KEY (application_id) REFERENCES ns_application (application_id),
  CONSTRAINT ns_otp_policy_fk FOREIGN KEY (otp_policy_id) REFERENCES ns_otp_policy (otp_policy_id)
);

-- Table ns_credential_storage stores credential values, counters and other data related to credentials.
CREATE TABLE ns_credential_storage (
  credential_id                    VARCHAR2(256 CHAR) NOT NULL PRIMARY KEY, -- Credential identifier (generated by application as UUID).
  credential_definition_id         NUMBER(19,0) NOT NULL,                   -- Credential definition identifier.
  user_id                          VARCHAR2(256 CHAR) NOT NULL,             -- User identity identifier.
  type                             VARCHAR2(32 CHAR) NOT NULL,              -- Credential type: PERMANENT, TEMPORARY.
  user_name                        VARCHAR2(256 CHAR),                      -- Username.
  value                            VARCHAR2(256 CHAR) NOT NULL,             -- Credential value.
  status                           VARCHAR2(32 CHAR) NOT NULL,              -- Credential status: ACTIVE, BLOCKED_TEMPORARY, BLOCKED_PERMANENT, REMOVED.
  attempt_counter                  NUMBER(19,0) DEFAULT 0 NOT NULL,         -- Attempt counter for both successful and failed attempts.
  failed_attempt_counter_soft      NUMBER(19,0) DEFAULT 0 NOT NULL,         -- Soft failed attempt counter.
  failed_attempt_counter_hard      NUMBER(19,0) DEFAULT 0 NOT NULL,         -- Hard failed attempt counter.
  encryption_algorithm             VARCHAR2(256 CHAR),                      -- Encryption algorithm used for encrypting credential value.
  hashing_config_id                NUMBER(19,0),                            -- Hashing configuration used when credential value was hashed.
  timestamp_created                TIMESTAMP,                               -- Timestamp when credential was created.
  timestamp_expires                TIMESTAMP,                               -- Timestamp when credential expires.
  timestamp_blocked                TIMESTAMP,                               -- Timestamp when credential was blocked.
  timestamp_last_updated           TIMESTAMP,                               -- Timestamp when credential was last updated.
  timestamp_last_credential_change TIMESTAMP,                               -- Timestamp when credential value was last changed.
  timestamp_last_username_change   TIMESTAMP,                               -- Timestamp when username value was last changed.
  CONSTRAINT ns_credential_definition_fk FOREIGN KEY (credential_definition_id) REFERENCES ns_credential_definition (credential_definition_id),
  CONSTRAINT ns_credential_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_credential_history stores historical values of credentials.
CREATE TABLE ns_credential_history (
  credential_history_id       NUMBER(19,0) NOT NULL PRIMARY KEY,            -- Credential history identifier (autogenerated).
  credential_definition_id    NUMBER(19,0) NOT NULL,                        -- Credential identifier.
  user_id                     VARCHAR2(256 CHAR) NOT NULL,                  -- User identity identifier.
  user_name                   VARCHAR2(256 CHAR),                           -- Username.
  value                       VARCHAR2(256 CHAR) NOT NULL,                  -- Credential value.
  encryption_algorithm        VARCHAR2(256 CHAR),                           -- Encryption algorithm used for encrypting credential value.
  hashing_config_id           NUMBER(19,0),                                 -- Hashing configuration used when credential value was hashed.
  timestamp_created           TIMESTAMP,                                    -- Timestamp when credential was created.
  CONSTRAINT ns_credential_history_definition_fk FOREIGN KEY (credential_definition_id) REFERENCES ns_credential_definition (credential_definition_id),
  CONSTRAINT ns_credential_history_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id)
);

-- Table ns_otp_storage stores one time password values, counters and other data related to one time passwords.
CREATE TABLE ns_otp_storage (
  otp_id                      VARCHAR2(256 CHAR) NOT NULL PRIMARY KEY,      -- One time password identifier (generated by application as UUID).
  otp_definition_id           NUMBER(19,0) NOT NULL,                        -- One time password definition identifier.
  user_id                     VARCHAR2(256 CHAR),                           -- User identifier.
  credential_definition_id    NUMBER(19,0),                                 -- Credential definition identifier used when updating failed counter.
  operation_id                VARCHAR2(256 CHAR),                           -- Operation identifier.
  value                       VARCHAR2(256 CHAR),                           -- One time password value.
  salt                        BLOB,                                         -- Cryptographic salt used when generating one time password.
  status                      VARCHAR2(32 CHAR) NOT NULL,                   -- One time password status: ACTIVE, USED, BLOCKED, REMOVED.
  otp_data                    CLOB,                                         -- Data used for generating one time password.
  attempt_counter             NUMBER(19,0) DEFAULT 0 NOT NULL,              -- One time password attempt counter.
  failed_attempt_counter      NUMBER(19,0) DEFAULT 0 NOT NULL,              -- One time password failed attempt counter.
  encryption_algorithm        VARCHAR2(256 CHAR),                           -- Encryption algorithm used for encrypting OTP value.
  timestamp_created           TIMESTAMP,                                    -- Timestamp when one time password was created.
  timestamp_verified          TIMESTAMP,                                    -- Timestamp when one time password was verified.
  timestamp_blocked           TIMESTAMP,                                    -- Timestamp when one time password was blocked.
  timestamp_expires           TIMESTAMP,                                    -- Timestamp when one time password expires.
  CONSTRAINT ns_otp_definition_fk FOREIGN KEY (otp_definition_id) REFERENCES ns_otp_definition (otp_definition_id)
);

-- Table ns_operation stores details of Web Flow operations.
-- Only the last status is stored in this table, changes of operations are stored in table ns_operation_history.
CREATE TABLE ns_operation (
  operation_id                  VARCHAR2(256 CHAR) PRIMARY KEY NOT NULL,  -- ID of a specific operation instance, random value in the UUID format or any value that external system decides to set as the operation ID when creating the operation.
  operation_name                VARCHAR2(32 CHAR) NOT NULL,               -- Name of the operation, represents a type of the operation, for example, "login" or "authorize_payment".
  operation_data                CLOB NOT NULL,                            -- Signing data of the operation.
  operation_form_data           CLOB,                                     -- Structured data of the operation that are displayed to the end user.
  application_id                VARCHAR2(256 CHAR),                       -- ID of the application that initiated the operation, usually OAuth 2.1 client ID.
  application_name              VARCHAR2(256 CHAR),                       -- Displayable name of the application that initiated the operation.
  application_description       VARCHAR2(256 CHAR),                       -- Displayable description of the application that initiated the operation.
  application_original_scopes   VARCHAR2(256 CHAR),                       -- Original OAuth 2.1 scopes used by the application that initiated the operation.
  application_extras            CLOB,                                     -- Any additional information related to the application that initiated the operation.
  user_id                       VARCHAR2(256 CHAR),                       -- Associated user ID.
  organization_id               VARCHAR2(256 CHAR),                       -- Associated organization ID.
  user_account_status           VARCHAR2(32 CHAR),                        -- Status of the user account while initiated the operation - ACTIVE, NOT_ACTIVE.
  external_operation_name       VARCHAR2(32 CHAR),                        -- External operation name, which can further specify the operation purpose.
  external_transaction_id       VARCHAR2(256 CHAR),                       -- External transaction ID, for example ID of a payment in a transaction system.
  result                        VARCHAR2(32 CHAR),                        -- Operation result - CONTINUE, FAILED, DONE.
  timestamp_created             TIMESTAMP,                                -- Timestamp when this operation was created.
  timestamp_expires             TIMESTAMP,                                -- Timestamp of the expiration of the operation.
  CONSTRAINT ns_operation_organization_fk FOREIGN KEY (organization_id) REFERENCES ns_organization (organization_id),
  CONSTRAINT ns_operation_config_fk FOREIGN KEY (operation_name) REFERENCES ns_operation_config (operation_name)
);

-- Table ns_authentication stores authentication attempts.
CREATE TABLE ns_authentication (
  authentication_id           VARCHAR2(256 CHAR) NOT NULL PRIMARY KEY,      -- Authentication identifier (autogenerated).
  user_id                     VARCHAR2(256 CHAR),                           -- User identity identifier.
  type                        VARCHAR2(32 CHAR) NOT NULL,                   -- Authentication type: CREDENTIAL, OTP, CREDENTIAL_OTP.
  credential_id               VARCHAR2(256 CHAR),                           -- Credential identifier.
  otp_id                      VARCHAR2(256 CHAR),                           -- One time password identifier.
  operation_id                VARCHAR2(256 CHAR),                           -- Operation identifier.
  result                      VARCHAR2(32 CHAR) NOT NULL,                   -- Overall authentication result.
  result_credential           VARCHAR2(32 CHAR),                            -- Authentication result for credential authentication.
  result_otp                  VARCHAR2(32 CHAR),                            -- Authentication result for one time password authentication.
  timestamp_created           TIMESTAMP,                                    -- Timestamp when authentication record was created.
  CONSTRAINT ns_auth_credential_fk FOREIGN KEY (credential_id) REFERENCES ns_credential_storage (credential_id),
  CONSTRAINT ns_auth_otp_fk FOREIGN KEY (otp_id) REFERENCES ns_otp_storage (otp_id),
  CONSTRAINT ns_auth_operation_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id)
);

-- Table ns_operation_history stores all changes of operations.
CREATE TABLE ns_operation_history (
  operation_id                VARCHAR2(256 CHAR) NOT NULL,                -- Operation ID.
  result_id                   INTEGER NOT NULL,                           -- Result ordering index identifier, incrementing value, starts with 1.
  request_auth_method         VARCHAR2(32 CHAR) NOT NULL,                 -- Authentication method used for the step.
  request_auth_instruments    VARCHAR2(256 CHAR),                         -- Which specific instruments were used for the step. Supported values are: PASSWORD, OTP_KEY, POWERAUTH_TOKEN, HW_TOKEN. There can be multiple supported instruments, they are stored encoded in JSON format.
  request_auth_step_result    VARCHAR2(32 CHAR) NOT NULL,                 -- Authentication result: CANCELED, AUTH_METHOD_FAILED, AUTH_FAILED, CONFIRMED
  request_params              VARCHAR2(4000 CHAR),                        -- Additional request parameters.
  response_result             VARCHAR2(32 CHAR) NOT NULL,                 -- Authentication step result: FAILED, CONTINUE, DONE.
  response_result_description VARCHAR2(256 CHAR),                         -- Additional information about the authentication step result.
  response_steps              VARCHAR2(4000 CHAR),                        -- Information about which methods are allowed in the next step.
  response_timestamp_created  TIMESTAMP,                                  -- Timestamp when the record was created.
  response_timestamp_expires  TIMESTAMP,                                  -- Timestamp when the operation step should expire.
  chosen_auth_method          VARCHAR2(32 CHAR),                          -- Information about which authentication method was chosen, in case user can chose the authentication method.
  mobile_token_active         NUMBER(1) DEFAULT 0 NOT NULL,               -- Information about if mobile token is active during the particular authentication step, in order to show the mobile token operation at the right time.
  authentication_id           VARCHAR2(256 CHAR),                         -- Reference to the authentication record.
  pa_operation_id             VARCHAR2(256 CHAR),                         -- PowerAuth operation ID for PowerAuth operations.
  pa_auth_context             VARCHAR2(256 CHAR),                         -- PowerAuth operation ID for PowerAuth operations.
  CONSTRAINT ns_history_pk PRIMARY KEY (operation_id, result_id),
  CONSTRAINT ns_history_operation_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id),
  CONSTRAINT ns_history_auth_method_fk FOREIGN KEY (request_auth_method) REFERENCES ns_auth_method (auth_method),
  CONSTRAINT ns_history_chosen_method_fk FOREIGN KEY (chosen_auth_method) REFERENCES ns_auth_method (auth_method),
  CONSTRAINT ns_history_authentication_fk FOREIGN KEY (authentication_id) REFERENCES ns_authentication (authentication_id)
);

-- Table ns_operation_afs stores AFS requests and responses.
CREATE TABLE ns_operation_afs (
  afs_action_id               INTEGER PRIMARY KEY NOT NULL,               -- ID of the AFS action.
  operation_id                VARCHAR2(256 CHAR) NOT NULL,                -- Operation ID.
  request_afs_action          VARCHAR2(256 CHAR) NOT NULL,                -- Information about requested AFS action.
  request_step_index          INTEGER NOT NULL,                           -- Counter within the specific operation step that is associated with AFS action, e.g. to differentiate multiple authentication attempts. Incrementing value, starts with 1.
  request_afs_extras          VARCHAR2(256 CHAR),                         -- Additional information about AFS action, typically a cookie values used in AFS system.
  response_afs_apply          NUMBER(1) DEFAULT 0 NOT NULL,               -- Response information about if AFS was applied.
  response_afs_label          VARCHAR2(256 CHAR),                         -- Response AFS label (information about what should the application do).
  response_afs_extras         VARCHAR2(256 CHAR),                         -- Additional information sent in AFS response.
  timestamp_created           TIMESTAMP,                                  -- Timestamp this AFS action was created.
  CONSTRAINT ns_operation_afs_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id)
);

-- Table ns_user_prefs stores user preferences.
-- Status of authentication methods is stored in this table per user (methods can be enabled or disabled).
CREATE TABLE ns_user_prefs (
  user_id       VARCHAR2(256 CHAR) PRIMARY KEY NOT NULL,      -- User ID.
  auth_method_1 NUMBER(1) DEFAULT 0,                          -- Flag indicating if "authentication method 1" is enabled.
  auth_method_2 NUMBER(1) DEFAULT 0,                          -- Flag indicating if "authentication method 2" is enabled.
  auth_method_3 NUMBER(1) DEFAULT 0,                          -- Flag indicating if "authentication method 3" is enabled.
  auth_method_4 NUMBER(1) DEFAULT 0,                          -- Flag indicating if "authentication method 4" is enabled.
  auth_method_5 NUMBER(1) DEFAULT 0,                          -- Flag indicating if "authentication method 5" is enabled.
  auth_method_1_config VARCHAR2(256 CHAR),                    -- Configuration for "authentication method 1".
  auth_method_2_config VARCHAR2(256 CHAR),                    -- Configuration for "authentication method 2".
  auth_method_3_config VARCHAR2(256 CHAR),                    -- Configuration for "authentication method 3".
  auth_method_4_config VARCHAR2(256 CHAR),                    -- Configuration for "authentication method 4".
  auth_method_5_config VARCHAR2(256 CHAR)                     -- Configuration for "authentication method 5".
);

-- Table da_sms_authorization stores data for SMS OTP authorization.
CREATE TABLE da_sms_authorization (
  message_id           VARCHAR2(256 CHAR) PRIMARY KEY NOT NULL,           -- SMS message ID, ID of SMS OTP.
  operation_id         VARCHAR2(256 CHAR) NOT NULL,                       -- Operation ID.
  user_id              VARCHAR2(256 CHAR) NOT NULL,                       -- User ID.
  organization_id      VARCHAR2(256 CHAR),                                -- Organization ID.
  operation_name       VARCHAR2(32 CHAR) NOT NULL,                        -- Name of the operation that triggered the SMS (login, authorize_payment, ...).
  authorization_code   VARCHAR2(32 CHAR) NOT NULL,                        -- Value of the authorization code sent in the SMS.
  salt                 BLOB NOT NULL,                                     -- Salt used for authorization code calculation.
  message_text         CLOB NOT NULL,                                     -- Full SMS message text.
  verify_request_count INTEGER,                                           -- Number of verification attempts.
  verified             NUMBER(1) DEFAULT 0,                               -- Flag indicating if this SMS OTP was successfully verified.
  timestamp_created    TIMESTAMP,                                         -- Timestamp when the SMS OTP was generated.
  timestamp_verified   TIMESTAMP,                                         -- Timestamp when the SMS OTP was successfully validated.
  timestamp_expires    TIMESTAMP                                          -- Timestamp when the SMS OTP expires.
);

-- Table da_user_credentials stores built-in users for the data adapter
CREATE TABLE da_user_credentials (
  user_id               VARCHAR2(128 CHAR) PRIMARY KEY NOT NULL,          -- User ID. Technical identifier of the user.
  username              VARCHAR2(256 CHAR) NOT NULL,                      -- Username, the displayable value that users use to sign in.
  password_hash         VARCHAR2(256 CHAR) NOT NULL,                      -- Bcrypt hash of the password.
  family_name           VARCHAR2(256 CHAR) NOT NULL,                      -- User family name.
  given_name            VARCHAR2(256 CHAR) NOT NULL,                      -- User given name.
  organization_id       VARCHAR2(64 CHAR)  NOT NULL,                      -- User organization ID.
  phone_number          VARCHAR2(256 CHAR) NOT NULL                       -- Full phone number, should be stored in format that allows easy SMS message sending.
);

-- Table for the list of consent templates
CREATE TABLE tpp_consent (
  consent_id            VARCHAR2(64 CHAR) PRIMARY KEY NOT NULL,           -- Consent ID.
  consent_name          VARCHAR2(128 CHAR) NOT NULL,                      -- Consent name, localization key or full displayable value.
  consent_text          CLOB NOT NULL,                                    -- Consent text, localization key or full displayable value with optional placeholders.
  version               INT NOT NULL                                      -- Consent version.
);

-- Table for the list of consent currently given by a user
CREATE TABLE tpp_user_consent (
    id                  INTEGER PRIMARY KEY NOT NULL,                     -- User given consent ID.
    user_id             VARCHAR2(256 CHAR) NOT NULL,                      -- User ID.
    client_id           VARCHAR2(256 CHAR) NOT NULL,                      -- OAuth 2.1 client ID.
    consent_id          VARCHAR2(64 CHAR) NOT NULL,                       -- Consent ID.
    external_id         VARCHAR2(256 CHAR),                               -- External ID associated with the consent approval, usually the operation ID.
    consent_parameters  CLOB NOT NULL,                                    -- Specific parameters that were filled in into the user consent template.
    timestamp_created   TIMESTAMP,                                        -- Timestamp the consent with given ID was first created.
    timestamp_updated   TIMESTAMP                                         -- Timestamp the consent with given ID was given again before it was revoked (updated, prolonged).
);

-- Table for the list of changes in consent history by given user
CREATE TABLE tpp_user_consent_history (
    id                  INTEGER PRIMARY KEY NOT NULL,                     -- ID of the consent history record.
    user_id             VARCHAR2(256 CHAR) NOT NULL,                      -- User ID.
    client_id           VARCHAR2(256 CHAR) NOT NULL,                      -- Client ID.
    consent_id          VARCHAR2(64 CHAR) NOT NULL,                       -- Consent ID.
    consent_change      VARCHAR2(16 CHAR) NOT NULL,                       -- Type of the consent change: APPROVE, PROLONG, REJECT
    external_id         VARCHAR2(256 CHAR),                               -- External ID that was responsible for this specific consent change, usually the operation ID.
    consent_parameters  CLOB NOT NULL,                                    -- Specific parameters that were filled in into the user consent template in this consent change.
    timestamp_created   TIMESTAMP                                         -- Timestamp of the consent change.
);

CREATE TABLE tpp_detail (
  tpp_id                INTEGER PRIMARY KEY NOT NULL,                     -- ID of the TPP provider.
  tpp_name              VARCHAR2(256 CHAR) NOT NULL,                      -- Name of the TPP provider.
  tpp_license           VARCHAR2(256 CHAR) NOT NULL,                      -- Information about the TPP license.
  tpp_info              CLOB NULL,                                        -- Additional information about the TPP provider, if available.
  tpp_address           CLOB NULL,                                        -- TPP address, if available.
  tpp_website           CLOB NULL,                                        -- TPP website, if available.
  tpp_phone             VARCHAR2(256 CHAR) NULL,                          -- TPP phone number, if available.
  tpp_email             VARCHAR2(256 CHAR) NULL,                          -- TPP e-mail, if available.
  tpp_logo              BLOB NULL,                                        -- TPP logo, if available.
  tpp_blocked           NUMBER(1) DEFAULT 0 NOT NULL                      -- Indication if this TPP provider is blocked or not.
);

CREATE TABLE tpp_app_detail (
  tpp_id                INTEGER NOT NULL,                                 -- TPP ID.
  app_client_id         VARCHAR2(256 CHAR) NOT NULL,                      -- TPP app ID, represented as OAuth 2.1 client ID and connecting the application to OAuth 2.1 credentials.
  app_name              VARCHAR2(256 CHAR) NOT NULL,                      -- TPP app name.
  app_info              CLOB NULL,                                        -- An arbitrary additional info about TPP app, if available.
  app_type              VARCHAR2(32 CHAR) NULL,                           -- Application type, "web" or "native".
  CONSTRAINT tpp_detail_pk PRIMARY KEY (tpp_id, app_client_id),
  CONSTRAINT tpp_detail_fk FOREIGN KEY (tpp_id) REFERENCES tpp_detail (tpp_id),
  CONSTRAINT tpp_client_secret_fk FOREIGN KEY (app_client_id) REFERENCES oauth_client_details (client_id)
);

-- Table audit_log stores auditing information
BEGIN EXECUTE IMMEDIATE 'CREATE TABLE audit_log (
    audit_log_id       VARCHAR2(36 CHAR) PRIMARY KEY,
    application_name   VARCHAR2(256 CHAR) NOT NULL,
    audit_level        VARCHAR2(32 CHAR) NOT NULL,
    audit_type         VARCHAR2(256 CHAR),
    timestamp_created  TIMESTAMP,
    message            CLOB NOT NULL,
    exception_message  CLOB,
    stack_trace        CLOB,
    param              CLOB,
    calling_class      VARCHAR2(256 CHAR) NOT NULL,
    thread_name        VARCHAR2(256 CHAR) NOT NULL,
    version            VARCHAR2(256 CHAR),
    build_time         TIMESTAMP
)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

-- Table audit_param stores auditing parameters
BEGIN EXECUTE IMMEDIATE 'CREATE TABLE audit_param (
    audit_log_id       VARCHAR2(36 CHAR),
    timestamp_created  TIMESTAMP,
    param_key          VARCHAR2(256 CHAR),
    param_value        VARCHAR2(4000 CHAR)
)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

CREATE INDEX wf_operation_hash ON wf_operation_session (operation_hash);
CREATE INDEX wf_websocket_session ON wf_operation_session (websocket_session_id);
CREATE INDEX ns_operation_pending ON ns_operation (user_id, result);
CREATE UNIQUE INDEX ns_operation_afs_unique on ns_operation_afs (operation_id, request_afs_action, request_step_index);
CREATE INDEX wf_certificate_operation ON wf_certificate_verification (operation_id);
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
CREATE INDEX ns_user_role_role ON ns_user_role (role_id);
CREATE INDEX ns_user_alias_user_id ON ns_user_alias (user_id);
CREATE UNIQUE INDEX ns_credential_definition_name ON ns_credential_definition (name);
CREATE UNIQUE INDEX ns_otp_definition_name ON ns_otp_definition (name);
CREATE INDEX ns_credential_storage_user_id ON ns_credential_storage (user_id);
CREATE INDEX ns_credential_storage_status ON ns_credential_storage (status);
CREATE UNIQUE INDEX ns_credential_storage_query1 ON ns_credential_storage (CASE WHEN user_name IS NOT NULL THEN credential_definition_id || '&' || user_name END);
CREATE INDEX ns_credential_storage_query1_perf ON ns_credential_storage (credential_definition_id, user_name);
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

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_log_timestamp ON audit_log (timestamp_created)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_log_application ON audit_log (application_name)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_log_level ON audit_log (audit_level)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_log_type ON audit_log (audit_type)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_param_log ON audit_param (audit_log_id)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_param_timestamp ON audit_param (timestamp_created)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_param_key ON audit_param (param_key)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

BEGIN EXECUTE IMMEDIATE 'CREATE INDEX audit_param_value ON audit_param (param_value)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

-- Foreign keys for user identity, to be used only when all user identities are stored in Next Step
-- ALTER TABLE ns_operation ADD CONSTRAINT ns_operation_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id);
-- ALTER TABLE ns_user_prefs ADD CONSTRAINT ns_user_prefs_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id);
-- ALTER TABLE ns_otp_storage ADD CONSTRAINT ns_otp_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id);
-- ALTER TABLE ns_authentication ADD CONSTRAINT ns_auth_user_fk FOREIGN KEY (user_id) REFERENCES ns_user_identity (user_id);