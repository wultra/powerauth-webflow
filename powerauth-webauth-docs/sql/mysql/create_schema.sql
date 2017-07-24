DROP TABLE IF EXISTS ns_step_definition;
DROP TABLE IF EXISTS ns_operation_history;
DROP TABLE IF EXISTS ns_operation;
DROP TABLE IF EXISTS ns_user_prefs;
DROP TABLE IF EXISTS ns_auth_method;
DROP TABLE IF EXISTS oauth_code;
DROP TABLE IF EXISTS oauth_refresh_token;
DROP TABLE IF EXISTS oauth_access_token;
DROP TABLE IF EXISTS oauth_client_token;
DROP TABLE IF EXISTS oauth_client_details;

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
);

CREATE TABLE oauth_client_token (
  token_id          VARCHAR(256),
  token             LONG VARBINARY,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name         VARCHAR(256),
  client_id         VARCHAR(256)
);


CREATE TABLE oauth_access_token (
  token_id          VARCHAR(256),
  token             LONG VARBINARY,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name         VARCHAR(256),
  client_id         VARCHAR(256),
  authentication    LONG VARBINARY,
  refresh_token     VARCHAR(256)
);

CREATE TABLE oauth_refresh_token (
  token_id       VARCHAR(256),
  token          LONG VARBINARY,
  authentication LONG VARBINARY
);

CREATE TABLE oauth_code (
  code           VARCHAR(255),
  authentication LONG VARBINARY
);

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
);

CREATE TABLE ns_user_prefs (
  user_id       VARCHAR(256) PRIMARY KEY,
  auth_method_1 BOOLEAN,
  auth_method_2 BOOLEAN,
  auth_method_3 BOOLEAN,
  auth_method_4 BOOLEAN,
  auth_method_5 BOOLEAN
);

CREATE TABLE ns_operation (
  operation_id                  VARCHAR(256) PRIMARY KEY,
  operation_name                VARCHAR(32),
  operation_data                VARCHAR(4096),
  operation_display_details     TEXT,
  user_id                       VARCHAR(256),
  result                        VARCHAR(32),
  timestamp_created             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  timestamp_expires             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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
);

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
);
