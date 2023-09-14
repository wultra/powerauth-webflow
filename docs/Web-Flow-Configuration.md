# PowerAuth Web Flow Configuration

This chapter describes configuration of Web Flow.

See the [installation guide](./Web-Flow-Installation-Manual.md#update-application-configurations) for instructions how to update the configuration properties.

## Web Flow Server

At minimum the following configuration properties should be updated based on deployment:
- `powerauth.dataAdapter.service.url` - Data Adapter service URL
- `powerauth.nextstep.service.url` - Next Step service URL
- `powerauth.service.url` - PowerAuth service URL
- `powerauth.push.service.url` - PowerAuth Push service URL
- `powerauth.webflow.page.ext-resources.location` - path to customized resources (use file:/path/to/your/ext-resources instead of classpath for location on disk)
- database configuration - see examples below

Complete configuration file:
```properties
# Allow externalization of properties using application-ext.properties
spring.profiles.active=ext

# Data Adapter Server Service URL
powerauth.dataAdapter.service.url=http://localhost:8080/powerauth-data-adapter

# Next Step Server Service URL
powerauth.nextstep.service.url=http://localhost:8080/powerauth-nextstep

# PowerAuth Server URL
powerauth.service.url=http://localhost:8080/powerauth-java-server/rest
powerauth.service.security.clientToken=
powerauth.service.security.clientSecret=
# Whether invalid SSL certificates should be accepted
powerauth.service.ssl.acceptInvalidSslCertificate=false

# PowerAuth Push Server URL
powerauth.push.service.url=http://localhost:8080/powerauth-push-server

# Dynamic CSS stylesheet URL
powerauth.webflow.page.title=PowerAuth Web Flow
powerauth.webflow.page.ext-resources.location=classpath:/static/resources/
powerauth.webflow.page.ext-resources.cache-duration=1h
powerauth.webflow.page.custom-css.url=

# Database Configuration - PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.hikari.auto-commit=false
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - Oracle
#spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/powerauth
#spring.datasource.username=powerauth
#spring.datasource.password=
#spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Application Service Configuration
powerauth.webflow.service.applicationName=powerauth-webflow
powerauth.webflow.service.applicationDisplayName=PowerAuth Web Flow Server
powerauth.webflow.service.applicationEnvironment=

# Configuration of Offline Mode
powerauth.webflow.offlineMode.available=true

# Enable or disable operations support in PowerAuth server
powerauth.webflow.pa.operations.enabled=false

# Configuration of Android Security Warning
powerauth.webflow.android.showSecurityWarning=true

# Configuration of Optional User Password Encryption
powerauth.webflow.password.protection.type=NO_PROTECTION
powerauth.webflow.password.encryption.transformation=
powerauth.webflow.password.encryption.key=

# Configuration of authentication using temporary credentials
powerauth.webflow.authentication.allowTemporaryCredentials=false

# Configuration of Delay for Resending SMS in Milliseconds
powerauth.webflow.sms.resend.delayMs=60000

# Configuration of Delay for Showing Operation Timeout Warning in Milliseconds
powerauth.webflow.timeout.warning.delayMs=60000

# Configuration of Limit for Large Consent Panel in Number of Characters
powerauth.webflow.consent.limit.enabled=false
powerauth.webflow.consent.limit.characters=750

# Configuration of Operation Approval Signing using Certificates
powerauth.webflow.approval.certificate.enabled=false
powerauth.webflow.approval.certificate.signer=ICA_CLIENT_SIGN
powerauth.webflow.approval.certificate.signer.ica.configurationUrl=
powerauth.webflow.approval.certificate.signer.ica.logLevel=1
powerauth.webflow.approval.certificate.signer.ica.extensionOwner=
powerauth.webflow.approval.certificate.signer.ica.extensionIDChrome=
powerauth.webflow.approval.certificate.signer.ica.extensionIDOpera=
powerauth.webflow.approval.certificate.signer.ica.extensionIDEdge=
powerauth.webflow.approval.certificate.signer.ica.extensionIDFirefox=
powerauth.webflow.approval.certificate.signer.ica.extensionInstallURLFirefox=

# Anti-fraud System Configuration
powerauth.webflow.afs.enabled=false
powerauth.webflow.afs.type=THREAT_MARK
powerauth.webflow.afs.detectIpAddress=false
powerauth.webflow.afs.forceIpv4=true
powerauth.webflow.afs.tm.cookies.deviceTag=
powerauth.webflow.afs.tm.cookies.sessionSid=

# User Input Validation
powerauth.webflow.input.username.maxLength=256
powerauth.webflow.input.password.maxLength=128
powerauth.webflow.input.smsOtp.maxLength=8

# Configuration of CORS Requests for Client Certificate Verification
powerauth.webflow.security.cors.enabled=false
powerauth.webflow.security.cors.allowOrigin=https://localhost.cz

# OAuth 2.1 resource server introspection configuration
powerauth.webflow.service.oauth2.introspection.uri=http://localhost:8080/powerauth-webflow/oauth2/introspect
powerauth.webflow.service.oauth2.introspection.clientId=democlient
powerauth.webflow.service.oauth2.introspection.clientSecret=changeme

# Set JMX default domain in case JMX is enabled, otherwise the application startup fails due to clash in JMX bean names
spring.jmx.default-domain=powerauth-webflow

# Set Jackson date format
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ssZ

# Disable open session in view to avoid startup warning of Spring boot
spring.jpa.open-in-view=false

# Enable caching of static resources
spring.web.resources.cache.cachecontrol.max-age=86400

# Disable swagger-ui default petstore url
springdoc.swagger-ui.disable-swagger-default-url=true

# Set the Spring application name
spring.application.name=powerauth-webflow
```

Encryption of user passwords during transport (end-to-end encryption) can be configured using following properties:
```
# Configuration of Password Encryption
powerauth.webflow.password.protection.type=PASSWORD_ENCRYPTION_AES
powerauth.webflow.password.encryption.transformation=AES/CBC/PKCS7Padding
powerauth.webflow.password.encryption.key=[Secret Base64-encoded 32-bit key, you can generate it using code below and keep it secure]
```

The configuration specifies that user password should be encrypted using AES in CBC mode with PKCS#7 padding. 
The key is encoded using Base64 encoding and its size influences the strength of the encryption cipher. 
The cipher transformation can be configured using standard Java Cipher transformation definition.
See: https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html

The random encryption key can be generated using following code:
```java
byte[] randomBytes = new byte[32];
new SecureRandom().nextBytes(randomBytes);
String encryptionKey = Base64.getEncoder().encodeToString(randomBytes);
```

The symmetric key is used by both Web Flow and by the remote system which needs to decrypt the password for verification.
For information about password decryption, see: [User Password Encryption And Decryption](./Data-Adapter-REST-API-Reference.md#user-password-encryption-and-decryption)

## Next Step Server
At minimum the following configuration properties should be updated based on deployment:
- `powerauth.nextstep.operation.expirationTimeInSeconds` - operation expiration time in seconds
- `powerauth.dataAdapter.service.url` - Data Adapter service URL
- `powerauth.service.url` - PowerAuth service URL
- database configuration - see examples below

Complete configuration file:
```properties
# Allow externalization of properties using application-ext.properties
spring.profiles.active=ext

# Database Configuration - PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.hikari.auto-commit=false
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - Oracle
#spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/powerauth
#spring.datasource.username=powerauth
#spring.datasource.password=
#spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Data Adapter Server Service URL
powerauth.dataAdapter.service.url=http://localhost:8080/powerauth-data-adapter

# PowerAuth Server URL
powerauth.service.url=http://localhost:8080/powerauth-java-server/rest
powerauth.service.security.clientToken=
powerauth.service.security.clientSecret=
# Whether invalid SSL certificates should be accepted
powerauth.service.ssl.acceptInvalidSslCertificate=false

# Operation expiration time in seconds
powerauth.nextstep.operation.expirationTimeInSeconds=300

# Use original username for a removed credential when the credential is recreated
powerauth.nextstep.identity.credential.useOriginalUsername=false

# Maximum number of attempts when generating username
powerauth.nextstep.identity.credential.generateUsernameMaxAttempts=100

# Enable or disable operations support in PowerAuth server
powerauth.nextstep.pa.operations.enabled=false

# Key used for end-to-end encryption of credentials
powerauth.nextstep.e2eEncryption.key=

# Key used for database record encryption
powerauth.nextstep.db.master.encryption.key=

# Application Service Configuration
powerauth.nextstep.service.applicationName=powerauth-nextstep
powerauth.nextstep.service.applicationDisplayName=PowerAuth Next Step Server
powerauth.nextstep.service.applicationEnvironment=

# Set JMX default domain in case JMX is enabled, otherwise the application startup fails due to clash in JMX bean names
spring.jmx.default-domain=powerauth-nextstep

# Set Jackson date format
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ssZ

# Disable open session in view to avoid startup warning of Spring boot
spring.jpa.open-in-view=false

# Disable swagger-ui default petstore url
springdoc.swagger-ui.disable-swagger-default-url=true

# Set default media type for responses in REST API documentation
springdoc.default-produces-media-type=application/json

# Set the Spring application name
spring.application.name=powerauth-nextstep
```

Encryption of user passwords during transport (end-to-end encryption) can be configured using following property:
```properties
powerauth.nextstep.e2eEncryption.key=[Secret Base64-encoded 32-bit key, same as the key used in Web Flow]
```

Encryption of sensitive database records can be configured using following property:

```properties
powerauth.nextstep.db.master.encryption.key=[Secret Base64-encoded 32-bit key, you can generate it using code below and keep it secure]
```

The random encryption key can be generated using following code:
```java
byte[] randomBytes = new byte[32];
new SecureRandom().nextBytes(randomBytes);
String encryptionKey = Base64.getEncoder().encodeToString(randomBytes);
```

<!-- begin box warning -->
Do not use the same key for end-to-encryption and database record encryption. Store the keys securely, ideally using a vault mechanism._
<!-- end -->

## Data Adapter
At minimum the following configuration properties should be updated based on deployment:
- `powerauth.authorization.sms-otp.expiration-time-in-second` - SMS OTP operation expiration time in seconds
- `powerauth.authorization.sms-otp.max-verify-tries-per-message` - maximum number of attempts for SMS OTP verification
- database configuration - see examples below

Complete configuration file:
```properties
# Allow externalization of properties using application-ext.properties
spring.profiles.active=ext

# Database Configuration - PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.hikari.auto-commit=false
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - Oracle
#spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/powerauth
#spring.datasource.username=powerauth
#spring.datasource.password=
#spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# SMS OTP expiration time in seconds
powerauth.authorization.sms-otp.expiration-time-in-seconds=300
# Maximum number of tries to verify a SMS OTP authorization code
powerauth.authorization.sms-otp.max-verify-tries-per-message=5

# Set JMX default domain in case JMX is enabled, otherwise the application startup fails due to clash in JMX bean names
spring.jmx.default-domain=powerauth-data-adapter

# Application Service Configuration
powerauth.dataAdapter.service.applicationName=powerauth-data-adapter
powerauth.dataAdapter.service.applicationDisplayName=PowerAuth Data Adapter
powerauth.dataAdapter.service.applicationEnvironment=

# Disable open session in view to avoid startup warning of Spring boot
spring.jpa.open-in-view=false

# Disable swagger-ui default petstore url
springdoc.swagger-ui.disable-swagger-default-url=true
```

## Web Flow Client
At minimum the following configuration properties should be updated based on deployment:
- `powerauth.webflow.service.url` - Web Flow service URL
- `powerauth.webflow.service.oauth2.client.*` - OAuth 2.1 client configuration
- `powerauth.nextstep.service.url` - Next Step service URL

Complete configuration file:
```properties
# OAuth 2.1 client configuration
powerauth.webflow.service.oauth2.client.registrationId=democlient
powerauth.webflow.service.oauth2.client.id=democlient
powerauth.webflow.service.oauth2.client.secret=changeme
powerauth.webflow.service.oauth2.client.name=democlient
powerauth.webflow.service.oauth2.client.authorizationUri=http://localhost:8080/powerauth-webflow/oauth2/authorize
powerauth.webflow.service.oauth2.client.tokenUri=http://localhost:8080/powerauth-webflow/oauth2/token
powerauth.webflow.service.oauth2.client.userInfoUri=http://localhost:8080/powerauth-webflow/api/secure/profile/me/info
powerauth.webflow.service.oauth2.client.redirectUri=http://localhost:8080/powerauth-webflow-client/connect/demo
powerauth.webflow.service.oauth2.client.userNameAttributeName=id

# Next Step Server Service URL
powerauth.nextstep.service.url=http://localhost:8080/powerauth-nextstep

# Whether invalid SSL certificates should be accepted
powerauth.service.ssl.acceptInvalidSslCertificate=false

# Application Service Configuration
powerauth.webflow.client.service.applicationName=powerauth-webflow-client
powerauth.webflow.client.service.applicationDisplayName=PowerAuth Web Flow Client
powerauth.webflow.client.service.applicationEnvironment=

# Set JMX default domain in case JMX is enabled, otherwise the application startup fails due to clash in JMX bean names
spring.jmx.default-domain=powerauth-webflow-client

# Set Jackson date format
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ssZ

# Disable open session in view to avoid startup warning of Spring boot
spring.jpa.open-in-view=false

# Enable caching of static resources
spring.web.resources.cache.cachecontrol.max-age=86400
```

## OAuth 2.1 Client Configuration

Sample OAuth 2.1 client configuration:

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

Note: bcrypt('changeme', 12) => '$2a$12$MkYsT5igDXSDgRwyDVz1B.93h8F81E4GZJd/spy/1vhjM4CJgeed.'

You can use [htpasswd](https://httpd.apache.org/docs/2.4/programs/htpasswd.html) from Apache HTTP server to generate bcrypt hashes.

## AFS configuration

Anti-fraud system integration needs to be configured in table `ns_operation_config`.

Following parameters are configured for each AFS configuration:
- `afs_config_id` - unique identifier of AFS configuration which is configured in `ns_operation_config` table to link operation with AFS configuration
- `js_snippet_url` - URL which should be used for executing JavaScript code from anti-fraud system
- `parameters` - reserved for future use, specify value `{}`

AFS also needs to be configured using application properties:
- `powerauth.webflow.afs.enabled` - enables AFS integration
- `powerauth.webflow.afs.type` - AFS product type (e.g. `THREAT_MARK`)
- `powerauth.webflow.afs.detectIpAddress` - whether Web Flow should attempt to detect client IP addresses
- `powerauth.webflow.afs.forceIpv4` - whether only IPv4 addresses should be used when detecting client IP addresses
- `powerauth.webflow.afs.tm.cookies.deviceTag` - name of Threat Mark `deviceTag` cookie 
- `powerauth.webflow.afs.tm.cookies.sessionSid` - name of Threat Mark `sessionSid` cookie

## Authentication methods and next step definitions

Authentication methods and next step definitions need to be configured during Web Flow deployment.

See chapter [Configuring Next Step](Configuring-Next-Step.md) for details.

## Mobile token configuration

Mobile token needs to be enabled and configured in case it should be available for Web Flow. 

See chapter [Mobile Token Configuration](./Mobile-Token-Configuration.md) for details.
