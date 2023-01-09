-- Drop tables used for tests
DROP TABLE IF EXISTS oauth_client_details;
DROP TABLE IF EXISTS oauth_access_token;
DROP TABLE IF EXISTS oauth_refresh_token;

-- Create tables used for tests
CREATE TABLE oauth_client_details
(
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

CREATE TABLE oauth_access_token
(
    token_id          VARCHAR(256),
    token             LONGVARBINARY,
    authentication_id VARCHAR(256) PRIMARY KEY,
    user_name         VARCHAR(256),
    client_id         VARCHAR(256),
    authentication    LONGVARBINARY,
    refresh_token     VARCHAR(256)
);

create table oauth_refresh_token
(
    token_id       VARCHAR(256),
    token          LONGVARBINARY,
    authentication LONGVARBINARY
);

-- Insert OAuth client test data
INSERT INTO oauth_client_details (client_id, client_secret, scope, authorized_grant_types, web_server_redirect_uri, additional_information, autoapprove)
VALUES ('testclient', '$2a$12$MkYsT5igDXSDgRwyDVz1B.93h8F81E4GZJd/spy/1vhjM4CJgeed.', 'profile,aisp,pisp', 'authorization_code,refresh_token', 'http://localhost:8080/powerauth-webflow-client/connect/demo', '{}', 'true');
