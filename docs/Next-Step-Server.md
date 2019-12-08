# NextStep Server

Next Step Server resolves the Next Step of the authentication process.

You can obtain the war file which can be deployed to a Java EE container in [releases](https://github.com/wultra/powerauth-webflow/releases).

The configuration of Next Step Server is described in [Web Flow Configuration](./Web-Flow-Configuration.md).

Next step definitions need to be configured before deploying Next Step Server, see chapter [Configuring Next Step Definitions](./Configuring-Next-Step-Definitions.md).

## Next Step Server functionality

Next Step Server implements following functionality:
- management of next steps during authentication process
  - step definitions are loaded from database when Next Step Server starts
  - making decision about the next step of an operation given loaded step definitions, operation name, operation type, operation status and step authentication result
- management of authentication methods
  - listing available authentication methods both general and user-specific
  - enabling/disabling authentication methods per user including their configuration (configuration is required for the [Mobile Token](./Mobile-Token-Configuration.md))
- management of operations
  - create a new operation
  - update an operation based on next step decision
  - retrieve operation detail
  - list pending operations for user
  - retrieve operation configuration for an operation or all defined operations
  - update operation form data
  - update user and organization for an operation
  - update application context for an operation
  - set chosen authentication method
  - update mobile token status for an operation
  - retrieve mobile token configuration
  - store result of an AFS action
- management of organizations
  - list organizations
  - retrieve organization detail
- REST services are available for authentication methods and operations

The Next Step Server functionality is described in details in [Next Step Server REST API Reference](./Next-Step-Server-REST-API-Reference.md).
