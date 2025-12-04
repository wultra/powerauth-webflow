# NextStep Server

Next Step Server resolves the Next Step of the authentication process, handles authentication using credentials and OTP authorization codes, and manages user identities.

You can obtain the war file which can be deployed to a Java EE container in [releases](https://github.com/wultra/powerauth-webflow/releases), or use an OCI compatible image stored on [Docker Hub](https://hub.docker.com/r/powerauth/nextstep/)

```sh
docker pull powerauth/nextstep:latest
```

The configuration of Next Step Server is described in [Next Step Configuration](./Web-Flow-Configuration.md#next-step-server).

Next step definitions need to be configured before deploying Next Step Server, see chapter [Configuring Next Step](Configuring-Next-Step.md).

## Next Step Architecture

[Next Step Architecture Diagram](./img/NextStep_Architecture.png)

### Next Step Server Container

The container with Next Step Server application.

### SQL DB

Required external service. SQL compatible database. PostgreSQL, Oracle and MS SQL in LTS releases are supported.

### Customer Systems

The external systems calling the REST API of NextStep Server. The integration is always unidirectional.

### Next Step Server Init Container

The Next Step can be optionally deployed with init container. Wultra supplies the init container containing Liquibase database update scripts. The usage of the init container allows better control of deployment and also separation of a database user for schema modification from a user for application runtime.  

### Sidecar Container

The NextStep can be accessed via a sidecar container for advanced ingress management. This depends on the specific deployment and is not provided by Wultra.

### LDAP Server 

The Next Step can be configured to verify passwords in an external LDAP server.

### Data Adapter Service

See component [Data Adapter](./Data-Adapter.md). The Data Adapter provides optional connection to other 3rd party systems.

## Next Step Server functionality

Next Step Server implements following functionality:
- management of next steps during authentication process
  - step definitions are loaded from database when Next Step Server starts
  - making decision about the next step of an operation given loaded step definitions, operation name, operation type, operation status and step authentication result
- management of authentication methods
  - create an authentication method
  - listing available authentication methods both general and user-specific
  - enabling/disabling authentication methods per user including their configuration (configuration is required for the [Mobile Token](./Mobile-Token-Configuration.md))
  - get a list of authentication methods currently enabled for the user
  - delete an authentication method
- management of operations
  - create a new operation
  - update an operation based on the next step decision
  - retrieve operation detail
  - list pending operations for user
  - retrieve operation configuration for an operation or all defined operations
  - update operation form data
  - update user and organization for an operation
  - update application context for an operation
  - set chosen authentication method
  - update mobile token status for an operation
  - retrieve mobile token configuration
  - get and update detailed authentication method and operation configuration
  - authentication method downgrade for the next step
  - enable PowerAuth token for the next step
  - integration with PowerAuth operations
  - store result of an AFS action
- management of organizations
  - create an organization
  - list organizations
  - retrieve organization detail
  - delete an organization
- management of user identity
  - user identity CRUD operations
  - user contact CRUD operations
  - user alias CRUD operations
  - assignment of user roles
  - blocking and unblocking of user identity
  - obtaining user credential list
  - obtaining user authentication history
  - update status of multiple user identities
  - lookup of user identities
- management of Next Step applications
- management of credential policies
- management of OTP policies
- management of credential definitions
- management of OTP definitions
- configuration of hashing algorithm parameters
- secure storage of user credentials
- secure storage of OTP authorization codes, their delivery via Data Adapter and OTP state management
- storage of failed attempt counters, their update and reset
- configuration of next step definitions
- definition of user roles
- authentication using credentials, OTP authorization codes and combine credential and OTP authentication

REST services are available for all Next Step functionality listed above.

The Next Step Server functionality is described in details in [Next Step Server REST API Reference](./Next-Step-Server-REST-API-Reference.md).

## Monitoring

See [Monitoring guideline](Next-Step-Server-Monitoring.md) for detailed description how to monitor Next Step Server.