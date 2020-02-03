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
# Data Adapter Server Service URL
powerauth.dataAdapter.service.url=http://localhost:8080/powerauth-data-adapter

# Next Step Server Service URL
powerauth.nextstep.service.url=http://localhost:8080/powerauth-nextstep

# PowerAuth Server URL
powerauth.service.url=http://localhost:8080/powerauth-java-server/soap
powerauth.service.security.clientToken=
powerauth.service.security.clientSecret=
# Whether invalid SSL certificates should be accepted
powerauth.service.ssl.acceptInvalidSslCertificate=false

# PowerAuth Push Server URL
powerauth.push.service.url=http://localhost:8080/powerauth-push-server

# Dynamic CSS stylesheet URL
powerauth.webflow.page.title=PowerAuth Web Flow
powerauth.webflow.page.ext-resources.location=classpath:/static/resources/
powerauth.webflow.page.custom-css.url=

# Database Configuration - MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.connection.CharSet=utf8mb4
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - PostgreSQL
#spring.datasource.url=jdbc:postgresql://localhost:5432/powerauth
#spring.datasource.username=powerauth
#spring.datasource.password=powerauth
#spring.datasource.driver-class-name=org.postgresql.Driver
#spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
#spring.jpa.properties.hibernate.connection.characterEncoding=utf8
#spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - Oracle
#spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/powerauth
#spring.datasource.username=powerauth
#spring.datasource.password=
#spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
# The following properties speed up Spring Boot startup
#spring.jpa.database-platform=org.hibernate.dialect.Oracle12cDialect
#spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false

# Application Service Configuration
powerauth.webflow.service.applicationName=powerauth-webflow
powerauth.webflow.service.applicationDisplayName=PowerAuth Web Flow Server
powerauth.webflow.service.applicationEnvironment=

# Configuration of Offline Mode
powerauth.webflow.offlineMode.available=true

# Configuration of Android Security Warning
powerauth.webflow.android.showSecurityWarning=true

# Configuration of Optional User Password Encryption
powerauth.webflow.password.protection.type=NO_PROTECTION
powerauth.webflow.password.encryption.transformation=
powerauth.webflow.password.encryption.key=

# Configuration of Delay for Resending SMS in Milliseconds
powerauth.webflow.sms.resend.delayMs=60000

# Configuration of Delay for Showing Operation Timeout Warning in Milliseconds
powerauth.webflow.timeout.warning.delayMs=60000

# Configuration of Limit for Large Consent Panel in Number of Characters
powerauth.webflow.consent.limit.enabled=false
powerauth.webflow.consent.limit.characters=750

# Anti-fraud system configuration
powerauth.webflow.afs.enabled=false
powerauth.webflow.afs.type=THREAT_MARK
powerauth.webflow.afs.detectIpAddress=false
powerauth.webflow.afs.forceIpv4=true
powerauth.webflow.afs.tm.cookies.deviceTag=
powerauth.webflow.afs.tm.cookies.sessionSid=

# User input validation
powerauth.webflow.input.username.maxLength=256
powerauth.webflow.input.password.maxLength=128
powerauth.webflow.input.smsOtp.maxLength=8
```

Encryption of user passwords during transport can be configured using following properties:
```
# Configuration of Password Encryption
powerauth.webflow.password.protection.type=PASSWORD_ENCRYPTION_AES
powerauth.webflow.password.encryption.transformation=AES/CBC/PKCS7Padding
powerauth.webflow.password.encryption.key=[Secret Base 64 encoded 32-bit key, you generate it using code below and keep it secure]
```

The configuration specifies that user password should be encrypted using AES in CBC mode with PKCS#7 padding. 
The key is encoded using Base64 encoding and its size influences the strength of the encryption cipher. 
The cipher transformation can be configured using standard Java Cipher transformation definition.
See: https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html

The random encryption key can be generated using following code:
```java
byte[] randomBytes = new byte[32];
new SecureRandom().nextBytes(randomBytes);
String encryptionKey = BaseEncoding.base64().encode(randomBytes);
```

The symmetric key is used by both Web Flow and by the remote system which needs to decrypt the password for verification.
For information about password decryption, see: [User Password Encryption And Decryption](./Data-Adapter-REST-API-Reference.md#user-password-encryption-and-decryption)

## Next Step Server
At minimum the following configuration properties should be updated based on deployment:
- `powerauth.nextstep.operation.expirationTimeInSeconds` - operation expiration time in seconds
- database configuration - see examples below

Complete configuration file:
```properties
# Database Configuration - MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.connection.CharSet=utf8mb4
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - PostgreSQL
#spring.datasource.url=jdbc:postgresql://localhost:5432/powerauth
#spring.datasource.username=powerauth
#spring.datasource.password=
#spring.datasource.driver-class-name=org.postgresql.Driver
#spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
#spring.jpa.properties.hibernate.connection.characterEncoding=utf8
#spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - Oracle
#spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/powerauth
#spring.datasource.username=powerauth
#spring.datasource.password=
#spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
# The following properties speed up Spring Boot startup
#spring.jpa.database-platform=org.hibernate.dialect.Oracle12cDialect
#spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false

# Operation expiration time in seconds
powerauth.nextstep.operation.expirationTimeInSeconds=300

# Application Service Configuration
powerauth.nextstep.service.applicationName=powerauth-nextstep
powerauth.nextstep.service.applicationDisplayName=PowerAuth Next Step Server
powerauth.nextstep.service.applicationEnvironment=
```

## Data Adapter
At minimum the following configuration properties should be updated based on deployment:
- `powerauth.authorization.sms-otp.expiration-time-in-second` - SMS OTP operation expiration time in seconds
- `powerauth.authorization.sms-otp.max-verify-tries-per-message` - maximum number of attempts for SMS OTP verification
- database configuration - see examples below

Complete configuration file:
```properties
# Database Configuration - MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.connection.CharSet=utf8mb4
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - PostgreSQL
#spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
#spring.datasource.username=powerauth
#spring.datasource.password=
#spring.datasource.driver-class-name=org.postgresql.Driver
#spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
#spring.jpa.properties.hibernate.connection.characterEncoding=utf8
#spring.jpa.properties.hibernate.connection.useUnicode=true

# Database Configuration - Oracle
#spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/powerauth
#spring.datasource.username=powerauth
#spring.datasource.password=
#spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
# The following properties speed up Spring Boot startup
#spring.jpa.database-platform=org.hibernate.dialect.Oracle12cDialect
#spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false

# SMS OTP expiration time in seconds
powerauth.authorization.sms-otp.expiration-time-in-seconds=300
# Maximum number of tries to verify a SMS OTP authorization code
powerauth.authorization.sms-otp.max-verify-tries-per-message=5
```

## Web Flow Client
At minimum the following configuration properties should be updated based on deployment:
- `powerauth.webflow.service.url` - Web Flow service URL
- `powerauth.webflow.service.oauth.authorizeUrl` - OAuth 2.0 authorize endpoint
- `powerauth.webflow.service.oauth.tokenUrl` - OAuth 2.0 token endpoint
- `powerauth.webflow.service.oauth.clientId` - OAuth 2.0 client ID
- `powerauth.webflow.service.oauth.clientSecret` OAuth 2.0 client secret
- `powerauth.nextstep.service.url` - Next Step service URL

Complete configuration file:
```properties
# Web Flow Base URL Configuration
powerauth.webflow.service.url=http://localhost:8080/powerauth-webflow
powerauth.webflow.service.oauth.authorizeUrl=http://localhost:8080/powerauth-webflow/oauth/authorize
powerauth.webflow.service.oauth.tokenUrl=http://localhost:8080/powerauth-webflow/oauth/token
powerauth.webflow.service.oauth.clientId=democlient
powerauth.webflow.service.oauth.clientSecret=changeme

# Next Step Server Service URL
powerauth.nextstep.service.url=http://localhost:8080/powerauth-nextstep

# Whether invalid SSL certificates should be accepted
powerauth.service.ssl.acceptInvalidSslCertificate=false

# Application Service Configuration
powerauth.webflow.client.service.applicationName=powerauth-webflow-client
powerauth.webflow.client.service.applicationDisplayName=PowerAuth Web Flow Client
powerauth.webflow.client.service.applicationEnvironment=
```

## OAuth 2.0 Client Configuration

The OAuth 2.0 client needs to be configured in database during Web Flow deployment.

Change the client_id and client_secret in SQL snipped below. The scope, authorized_grant_types, additional_information and autoapprove values should not be changed.

Initialization of OAuth 2.0 client:
```sql
INSERT INTO oauth_client_details (client_id, client_secret, scope, authorized_grant_types, additional_information, autoapprove)
VALUES ('democlient', '$2a$12$MkYsT5igDXSDgRwyDVz1B.93h8F81E4GZJd/spy/1vhjM4CJgeed.', 'profile', 'authorization_code', '{}', 'true');
```

Note: bcrypt('changeme', 12) => '$2a$12$MkYsT5igDXSDgRwyDVz1B.93h8F81E4GZJd/spy/1vhjM4CJgeed.'

You can use [htpasswd](https://httpd.apache.org/docs/2.4/programs/htpasswd.html) from Apache HTTP server to generate bcrypt hashes.


## Organization configuration

Web Flow requires at least one organization configured. The default configuration is following:

Oracle:
```sql
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number) VALUES ('DEFAULT', null, 1, 1);
```

MySQL:
```sql
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number) VALUES ('DEFAULT', null, TRUE, 1);
```

The default configuration assigns the `DEFAULT` organization to all operations. You can define multiple organizations to support
authentication for multiple segments which can have overlapping user IDs, e.g.:

Oracle:
```sql
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number) VALUES ('RETAIL', 'organization.retail', 1, 1);
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number) VALUES ('SME', 'organization.sme', 0, 2);
```

MySQL:
```sql
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number) VALUES ('RETAIL', 'organization.retail', TRUE, 1);
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number) VALUES ('SME', 'organization.sme', FALSE, 2);
```

Such configuration defines two organizations `RETAIL` and `SME`. The user sees two tabs when authenticating with localized labels 
based on keys `organization.retail` and `organization.sme`. The user can switch the organization against which the authentication is performed. 
The `RETAIL` organization is the default one (it is preselected in the UI). The order of displayed organizations is defined as 
`RETAIL`, `SME` using the last parameter. 

_Warning: In case you configure multiple organizations make sure the user ID used in PowerAuth Web Flow, PowerAuth Server and PowerAuth Push Server is unique across all organizations and it is consistent in all PowerAuth backends. You can achieve this requirement by assigning unique user IDs in different organizations during user authentication. Alternatively the uniqueness requirement can be achieved by adding a prefix to all user IDs based on the organization against which the user was authenticated (e.g. `RETAIL.12345678`)._  

## Operation configuration

Operations need to be configured in table `ns_operation_config`.

Following parameters are configured for each operation:
- `operation_name` - unique operation name which identifies the operation
- `template_version` - template version, see documentation for [Operation Data](./Operation-Data.md)
  - currently used template version is `A`
- `template_id` - template identifier, see documentation for [Operation Data](./Operation-Data.md)
  - for approval (payment) use `1`
  - for login use `2`
- `mobile_token_enabled` - whether mobile token is enabled for this operation
- `mobile_token_mode` - configuration of mobile token factors, use only when `mobile_token_enabled` value is true
  - for 1FA use: `{"type":"1FA"}`
  - for 2FA use e.g.: `{"type":"2FA","variants":["possession_knowledge","possession_biometry"]}`
- `afs_enabled` - whether anti-fraud service integration is enabled for this operation
- `afs_config_id` - identifier of AFS configuration, use only when `afs_enabled` value is true

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

See chapter [Configuring Next Step Definitions](./Configuring-Next-Step-Definitions.md) for details.

## Mobile token configuration

Mobile token needs to be enabled and configured in case it should be available for Web Flow. 

See chapter [Mobile Token Configuration](./Mobile-Token-Configuration.md) for details.
