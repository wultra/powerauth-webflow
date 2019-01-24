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
# Credential Server Service URL
powerauth.dataAdapter.service.url=http://localhost:8080/powerauth-data-adapter

# Next Step Server Service URL
powerauth.nextstep.service.url=http://localhost:8080/powerauth-nextstep

# PowerAuth 2.0 Server URL
powerauth.service.url=http://localhost:8080/powerauth-java-server/soap
powerauth.service.security.clientToken=
powerauth.service.security.clientSecret=
# Whether invalid SSL certificates should be accepted
powerauth.service.ssl.acceptInvalidSslCertificate=false

# PowerAuth 2.0 Push Server URL
powerauth.push.service.url=http://localhost:8080/powerauth-push-server

# Dynamic CSS stylesheet URL
powerauth.webflow.page.title=PowerAuth 2.0 Web Flow
powerauth.webflow.page.ext-resources.location=classpath:/static/resources/
powerauth.webflow.page.custom-css.url=

# Database Keep-Alive
spring.datasource.test-while-idle=true
spring.datasource.test-on-borrow=true
spring.datasource.validation-query=SELECT 1

# Database Configuration - MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.connection.CharSet=utf8mb4
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

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
powerauth.webflow.service.applicationDisplayName=PowerAuth 2.0 Web Flow Server
powerauth.webflow.service.applicationEnvironment=

# Configuration of Offline mode
powerauth.webflow.offlineMode.available=true
```

## Next Step Server
At minimum the following configuration properties should be updated based on deployment:
- `powerauth.nextstep.operation.expirationTimeInSeconds` - operation expiration time in seconds
- database configuration - see examples below

Complete configuration file:
```properties
# Database Keep-Alive
spring.datasource.test-while-idle=true
spring.datasource.test-on-borrow=true
spring.datasource.validation-query=SELECT 1

# Database Configuration - MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.connection.CharSet=utf8mb4
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

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
powerauth.nextstep.service.applicationDisplayName=PowerAuth 2.0 Next Step Server
powerauth.nextstep.service.applicationEnvironment=
```

## Data Adapter
At minimum the following configuration properties should be updated based on deployment:
- `powerauth.authorization.sms-otp.expiration-time-in-second` - SMS OTP operation expiration time in seconds
- `powerauth.authorization.sms-otp.max-verify-tries-per-message` - maximum number of attempts for SMS OTP verification
- database configuration - see examples below

Complete configuration file:
```properties
# Database Keep-Alive
spring.datasource.test-while-idle=true
spring.datasource.test-on-borrow=true
spring.datasource.validation-query=SELECT 1

# Database Configuration - MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.connection.CharSet=utf8mb4
spring.jpa.properties.hibernate.connection.characterEncoding=utf8
spring.jpa.properties.hibernate.connection.useUnicode=true

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

# Application Service Configuration
powerauth.dataAdapter.service.applicationName=powerauth-data-adapter
powerauth.dataAdapter.service.applicationDisplayName=PowerAuth 2.0 Data Adapter
powerauth.dataAdapter.service.applicationEnvironment=
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
powerauth.webflow.client.service.applicationDisplayName=PowerAuth 2.0 Web Flow Client
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

## Authentication methods and next step definitions

Authentication methods and next step definitions need to be configured during Web Flow deployment.

See chapter [Configuring Next Step Definitions](./Configuring-Next-Step-Definitions.md) for details.
