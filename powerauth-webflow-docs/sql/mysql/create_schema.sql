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
  additional_information  VARCHAR(4096),
  autoapprove             VARCHAR(256)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table oauth_client_token stores OAuth2 tokens for retrieval by client applications.
-- See: https://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/client/token/JdbcClientTokenServices.html
CREATE TABLE oauth_client_token (
  token_id          VARCHAR(256),
  token             LONG VARBINARY,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name         VARCHAR(256),
  client_id         VARCHAR(256)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table oauth_access_token stores OAuth2 access tokens.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/token/store/JdbcTokenStore.java
CREATE TABLE oauth_access_token (
  token_id          VARCHAR(256),
  token             LONG VARBINARY,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name         VARCHAR(256),
  client_id         VARCHAR(256),
  authentication    LONG VARBINARY,
  refresh_token     VARCHAR(256)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table oauth_access_token stores OAuth2 refresh tokens.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/token/store/JdbcTokenStore.java
CREATE TABLE oauth_refresh_token (
  token_id       VARCHAR(256),
  token          LONG VARBINARY,
  authentication LONG VARBINARY
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table oauth_code stores data for the OAuth2 authorization code grant.
-- See: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/main/java/org/springframework/security/oauth2/provider/code/JdbcAuthorizationCodeServices.java
CREATE TABLE oauth_code (
  code           VARCHAR(255),
  authentication LONG VARBINARY
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_auth_method stores configuration of authentication methods.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_auth_method (
  auth_method        VARCHAR(32) PRIMARY KEY,
  order_number       INTEGER,
  check_user_prefs   BOOLEAN,
  user_prefs_column  INTEGER,
  user_prefs_default BOOLEAN,
  check_auth_fails   BOOLEAN,
  max_auth_fails     INTEGER,
  has_user_interface BOOLEAN,
  display_name_key   VARCHAR(32)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_user_prefs stores user preferences.
-- Status of authentication methods is stored in this able per user (methods can be enabled or disabled).
CREATE TABLE ns_user_prefs (
  user_id       VARCHAR(256) PRIMARY KEY,
  auth_method_1 BOOLEAN,
  auth_method_2 BOOLEAN,
  auth_method_3 BOOLEAN,
  auth_method_4 BOOLEAN,
  auth_method_5 BOOLEAN
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_operation stores details of Web Flow operations.
-- Only the last status is stored in this table, changes of operations are stored in table ns_operation_history.
CREATE TABLE ns_operation (
  operation_id              VARCHAR(256) PRIMARY KEY,
  operation_name            VARCHAR(32),
  operation_data            TEXT,
  operation_form_data       TEXT,
  user_id                   VARCHAR(256),
  result                    VARCHAR(32),
  timestamp_created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  timestamp_expires         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_operation_history stores all changes of operations.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_operation_history (
  operation_id                VARCHAR(256),
  result_id                   INTEGER,
  request_auth_method         VARCHAR(32),
  request_auth_step_result    VARCHAR(32),
  request_params              VARCHAR(4096),
  response_result             VARCHAR(32),
  response_result_description VARCHAR(256),
  response_steps              VARCHAR(4096),
  response_timestamp_created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  response_timestamp_expires  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (operation_id, result_id),
  FOREIGN KEY operation_fk (operation_id) REFERENCES ns_operation (operation_id),
  FOREIGN KEY auth_method_fk (request_auth_method) REFERENCES ns_auth_method (auth_method)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ns_step_definition stores definitions of authentication/authorization steps.
-- Data in this table needs to be loaded before Web Flow is started.
CREATE TABLE ns_step_definition (
  step_definition_id       INTEGER PRIMARY KEY,
  operation_name           VARCHAR(32),
  operation_type           VARCHAR(32),
  request_auth_method      VARCHAR(32),
  request_auth_step_result VARCHAR(32),
  response_priority        INTEGER,
  response_auth_method     VARCHAR(32),
  response_result          VARCHAR(32),
  FOREIGN KEY request_auth_method_fk (request_auth_method) REFERENCES ns_auth_method (auth_method),
  FOREIGN KEY response_auth_method_fk (response_auth_method) REFERENCES ns_auth_method (auth_method)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table ba_sms_authorization stores data for SMS OTP authorization.
CREATE TABLE ba_sms_authorization (
  message_id           VARCHAR(256) PRIMARY KEY,
  operation_id         VARCHAR(256),
  user_id              VARCHAR(256),
  operation_name       VARCHAR(32),
  authorization_code   VARCHAR(32),
  salt                 VARBINARY(16),
  message_text         TEXT,
  verify_request_count INTEGER,
  verified             BOOLEAN,
  timestamp_created    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  timestamp_verified   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  timestamp_expires    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
  accessToken VARCHAR(512) not null,
  secret VARCHAR(512),
  refreshToken VARCHAR(512),
  expireTime BIGINT,
PRIMARY KEY (userId, providerId, providerUserId));
CREATE UNIQUE INDEX UserConnectionRank on UserConnection(userId, providerId, rank);
*/
