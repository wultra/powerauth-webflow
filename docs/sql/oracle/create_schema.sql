-- Table oauth_client_details stores details about OAuth2 client applications.
-- Every Web Flow client application should have a record in this table.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/client/JdbcClientDetailsService.java
CREATE TABLE oauth_client_details (
  client_id               VARCHAR(256) PRIMARY KEY,
  resource_ids            VARCHAR(256),
  client_secret           VARCHAR(256),
  scope                   VARCHAR(256),
  authorized_grant_types  VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities             VARCHAR(256),
  access_token_validity   INTEGER,
  refresh_token_validity  INTEGER,
  additional_information  VARCHAR(4000),
  autoapprove             VARCHAR(256)
);

-- Table oauth_client_token stores OAuth2 tokens for retrieval by client applications.
-- See: https://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/client/token/JdbcClientTokenServices.html
CREATE TABLE oauth_client_token (
  authentication_id VARCHAR(256) PRIMARY KEY,
  token_id          VARCHAR(256),
  token             BLOB,
  user_name         VARCHAR(256),
  client_id         VARCHAR(256)
);

-- Table oauth_access_token stores OAuth2 access tokens.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/token/store/JdbcTokenStore.java
CREATE TABLE oauth_access_token (
  authentication_id VARCHAR(256) PRIMARY KEY,
  token_id          VARCHAR(256),
  token             BLOB,
  user_name         VARCHAR(256),
  client_id         VARCHAR(256),
  authentication    BLOB,
  refresh_token     VARCHAR(256)
);

-- Table oauth_access_token stores OAuth2 refresh tokens.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/token/store/JdbcTokenStore.java
CREATE TABLE oauth_refresh_token (
  token_id       VARCHAR(256),
  token          BLOB,
  authentication BLOB
);

-- Table oauth_code stores data for the OAuth2 authorization code grant.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/code/JdbcAuthorizationCodeServices.java
CREATE TABLE oauth_code (
  code           VARCHAR(255),
  authentication BLOB
);

-- Table ns_auth_method stores configuration of authentication methods.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_auth_method (
  auth_method        VARCHAR(32) PRIMARY KEY NOT NULL,
  order_number       INTEGER NOT NULL,
  check_user_prefs   NUMBER(1) DEFAULT 0 NOT NULL,
  user_prefs_column  INTEGER,
  user_prefs_default NUMBER(1) DEFAULT 0,
  check_auth_fails   NUMBER(1) DEFAULT 0 NOT NULL,
  max_auth_fails     INTEGER,
  has_user_interface NUMBER(1) DEFAULT 0,
  has_mobile_token   NUMBER(1) DEFAULT 0,
  display_name_key   VARCHAR(32)
);

-- Table ns_user_prefs stores user preferences.
-- Status of authentication methods is stored in this table per user (methods can be enabled or disabled).
CREATE TABLE ns_user_prefs (
  user_id       VARCHAR(256) PRIMARY KEY NOT NULL,
  auth_method_1 NUMBER(1) DEFAULT 0,
  auth_method_2 NUMBER(1) DEFAULT 0,
  auth_method_3 NUMBER(1) DEFAULT 0,
  auth_method_4 NUMBER(1) DEFAULT 0,
  auth_method_5 NUMBER(1) DEFAULT 0,
  auth_method_1_config VARCHAR(256),
  auth_method_2_config VARCHAR(256),
  auth_method_3_config VARCHAR(256),
  auth_method_4_config VARCHAR(256),
  auth_method_5_config VARCHAR(256)
);

-- Table ns_operation_config stores configuration of operations.
-- Each operation type (defined by operation_name) has a related mobile token template and configuration of signatures.
CREATE TABLE ns_operation_config (
  operation_name            VARCHAR(32) PRIMARY KEY NOT NULL,
  template_version          VARCHAR(1) NOT NULL,
  template_id               INTEGER NOT NULL,
  mobile_token_mode         VARCHAR(256) NOT NULL,
  afs_enabled               NUMBER(1) DEFAULT 0 NOT NULL
);

-- Table ns_organization stores definitions of organizations related to the operations.
-- At least one default organization must be configured.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_organization (
  organization_id          VARCHAR(256) PRIMARY KEY NOT NULL,
  display_name_key         VARCHAR(256),
  is_default               NUMBER(1) DEFAULT 0 NOT NULL,
  order_number             INTEGER NOT NULL
);

-- Table ns_operation stores details of Web Flow operations.
-- Only the last status is stored in this table, changes of operations are stored in table ns_operation_history.
CREATE TABLE ns_operation (
  operation_id              VARCHAR(256) PRIMARY KEY NOT NULL,
  operation_name            VARCHAR(32) NOT NULL,
  operation_data            CLOB NOT NULL,
  operation_form_data       CLOB,
  application_id            VARCHAR(256),
  application_name          VARCHAR(256),
  application_description   VARCHAR(256),
  application_extras        CLOB,
  user_id                   VARCHAR(256),
  organization_id           VARCHAR(256),
  result                    VARCHAR(32),
  timestamp_created         TIMESTAMP,
  timestamp_expires         TIMESTAMP,
  CONSTRAINT operation_organization_fk FOREIGN KEY (organization_id) REFERENCES ns_organization (organization_id)
);

-- Table ns_operation_history stores all changes of operations.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_operation_history (
  operation_id                VARCHAR(256) NOT NULL,
  result_id                   INTEGER NOT NULL,
  request_auth_method         VARCHAR(32) NOT NULL,
  request_auth_step_result    VARCHAR(32) NOT NULL,
  request_params              VARCHAR(4000),
  response_result             VARCHAR(32) NOT NULL,
  response_result_description VARCHAR(256),
  response_steps              VARCHAR(4000),
  response_timestamp_created  TIMESTAMP,
  response_timestamp_expires  TIMESTAMP,
  chosen_auth_method          VARCHAR(32),
  CONSTRAINT history_pk PRIMARY KEY (operation_id, result_id),
  CONSTRAINT history_operation_fk FOREIGN KEY (operation_id) REFERENCES ns_operation (operation_id),
  CONSTRAINT history_auth_method_fk FOREIGN KEY (request_auth_method) REFERENCES ns_auth_method (auth_method)
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

-- Table wf_operation_session maps operations to HTTP sessions.
-- Table is needed for handling of concurrent operations.
CREATE TABLE wf_operation_session (
  operation_id              VARCHAR(256) PRIMARY KEY NOT NULL,
  http_session_id           VARCHAR(256) NOT NULL,
  result                    VARCHAR(32) NOT NULL,
  timestamp_created         TIMESTAMP
);

-- Table da_sms_authorization stores data for SMS OTP authorization.
CREATE TABLE da_sms_authorization (
  message_id           VARCHAR(256) PRIMARY KEY NOT NULL,
  operation_id         VARCHAR(256) NOT NULL,
  user_id              VARCHAR(256) NOT NULL,
  organization_id      VARCHAR(256),
  operation_name       VARCHAR(32) NOT NULL,
  authorization_code   VARCHAR(32) NOT NULL,
  salt                 BLOB NOT NULL,
  message_text         CLOB NOT NULL,
  verify_request_count INTEGER,
  verified             NUMBER(1) DEFAULT 0,
  timestamp_created    TIMESTAMP,
  timestamp_verified   TIMESTAMP,
  timestamp_expires    TIMESTAMP
);

-- Table UserConnection is required only for the demo client application which is based on Spring Social.
-- See: https://github.com/spring-projects/spring-social
/*
CREATE TABLE UserConnection (
  userId VARCHAR(255) NOT NULL,
  providerId VARCHAR(255) NOT NULL,
  providerUserId VARCHAR(255),
  rank INTEGER NOT NULL,
  displayName VARCHAR(255),
  profileUrl VARCHAR(512),
  imageUrl VARCHAR(512),
  accessToken VARCHAR(512) NOT NULL,
  secret VARCHAR(512),
  refreshToken VARCHAR(512),
  expireTime NUMBER(19),
PRIMARY KEY (userId, providerId, providerUserId));
CREATE UNIQUE INDEX UserConnectionRank on UserConnection(userId, providerId, rank);
*/
