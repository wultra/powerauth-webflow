# Migration from 1.4.0 to 1.5.0

This guide contains instructions for migration from PowerAuth WebFlow version `1.4.x` to version `1.5.0`.

## Migration to Spring Boot 3

### Required Java Version

Web Flow requires Java 17 or higher due to migration to Spring Boot 3. Support for older Java versions is not available.

### Spring OAuth2 Migration

Due to migration to Spring Boot 3, following dependency changes have been introduced:

- Web Flow has been migrated to [Spring Authorization Server](https://github.com/spring-projects/spring-authorization-server).
- Web Flow user profile endpoint now uses [Spring Security OAuth2 Resource Server](https://github.com/spring-projects/spring-security/tree/main/oauth2/oauth2-resource-server).  
- The sample Web Flow client now uses [Spring Security OAuth2 Client](https://github.com/spring-projects/spring-security/tree/main/oauth2/oauth2-client).

Due to these changes, the OAuth version has been updated from 2.0 to version 2.1, with several limitations due to compatibility with OAuth 2.0:

- PXCE is not enforced to allow existing OAuth 2.0 clients to use new version of Web Flow.
- Default refresh token time to live remains 1 year. It is expected that Web Flow is used in backend-to-backend scenarios, and OAuth dance is not triggered directly from frontend applications, thus refresh token rotation is not enforced.
- The format of generated tokens remains opaque due to lower probability of information leakage which can be a problem with JWT tokens which may contain private data.

### Change of OAuth 2.x Endpoints

Due to migration to Spring Authorization Server, the OAuth endpoints URIs have been changed:

- Authorization endpoint: `/oauth2/authorize`
- Token endpoint: `/oauth2/token`
- Revocation endpoint: `/oauth2/revoke`
- Introspection endpoint: `/oauth2/introspect`

The user info endpoints URIs did not change.

There is a change in behaviour of revocation endpoint. Previously, the revocation endpoint revoked all access tokens as well as refresh token for a revoked access token. Currently, only the requested access token is revoked. In case you want to revoke all access tokens and refresh tokens, use the refresh token as the parameter during the revocation. 

## Database Changes

### Migration to Spring Authorization Server

Due to migration to Spring Authorization Server the database schema for OAuth 2.x support has changed.

Oracle:
```sql
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
```

PostgreSQL:
```sql
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

-- Table oauth2_registered_client stores information about OAuth 2.1 consents
-- Source: https://github.com/spring-projects/spring-authorization-server/blob/main/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql
CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);
```

### Migration of OAuth 2.x Clients

Due to migration to Spring Authorization Server, the OAuth clients need to be reconfigured.

The OAuth 2.1 client registration corresponds to existing records in table `oauth_client_details`. You can update the configuration based on the sample configuration below with any required customization.

Sample configuration:

```sql
INSERT INTO oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_secret_expires_at, client_name, client_authentication_methods, authorization_grant_types, redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings) VALUES ('8cb2cedf-0152-47c4-b25e-0ee81b1acd44', 'democlient', '2023-04-28 11:44:29.000000', '$2a$12$MkYsT5igDXSDgRwyDVz1B.93h8F81E4GZJd/spy/1vhjM4CJgeed.', null, 'democlient', 'client_secret_basic', 'authorization_code,refresh_token', 'http://localhost:8080/powerauth-webflow-client/connect/demo', null, 'profile,aisp,pisp', '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}', '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":true,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",300],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"reference"},"settings.token.refresh-token-time-to-live":["java.time.Duration",1296000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300]}}');
```

See Spring OAuth 2.1 client registration documentation for more details: https://docs.spring.io/spring-security/reference/servlet/oauth2/client/core.html#oauth2Client-client-registration

The following parameters should be configured:
- `id` - database row unique identifier
- `client_id` - identifier of the client
- `client_id_issued_at` - timestamp of client issuance 
- `client_secret` - client password hashed using bcrypt
- `client_name` - name of the client
- `redirect_uris` - registered redirect URIs for redirect to original application
- `scope` - supported OAuth 2.1 scopes
- `settings.token.access-token-time-to-live` - access token time to live
- `settings.token.refresh-token-time-to-live` - refresh token time to live
- `settings.token.authorization-code-time-to-live` - authorization code time to live

The remaining parameters should not be changed, because they correspond to the Spring Authorization Server integration.

You can use the following SQL query for automating client migration:

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
INSERT INTO oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_secret_expires_at, client_name, client_authentication_methods, authorization_grant_types, redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings) 
SELECT uuid_generate_v4(), client_id, now(), client_secret, null, client_id, 'client_secret_basic', authorized_grant_types, web_server_redirect_uri, null, scope, '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}', '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":true,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",300],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"reference"},"settings.token.refresh-token-time-to-live":["java.time.Duration",1296000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300]}}' FROM oauth_client_details;
```

### Updated Foreign Keys


Due to migration to Spring Authorization Server the following migration is necessary.

```sql```
ALTER TABLE tpp_app_detail DROP CONSTRAINT tpp_client_secret_fk;
ALTER TABLE tpp_app_detail ADD CONSTRAINT tpp_client_secret_fk FOREIGN KEY (app_client_id) REFERENCES oauth2_registered_client (client_id);
```

### Dropped Tables

Due to migration to Spring Authorization Server the following tables are no longer required:

```sql
DROP TABLE oauth_code;
DROP TABLE oauth_refresh_token;
DROP TABLE oauth_access_token;
DROP TABLE oauth_client_token;
DROP TABLE oauth_client_details;
```

### Dropped MySQL Support

Since version `1.5.0`, MySQL database is not supported anymore.


## Dependencies

PostgreSQL JDBC driver is already included in the WAR file.
Oracle JDBC driver remains optional and must be added to your deployment if desired.
