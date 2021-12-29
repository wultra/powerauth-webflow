# PowerAuth Web Flow

Web Flow is the main component which serves as an OAuth 2.0 provider.

You can obtain the war file which can be deployed to a Java EE container in [releases](https://github.com/wultra/powerauth-webflow/releases).

The configuration of Web Flow is described in [Web Flow Configuration](./Web-Flow-Configuration.md).

## Web Flow Functionality

Web Flow implements following functionality:
- frontend application the user interacts with during the authentication process
- backend REST services the frontend communicates with to handle user input and operation workflow
- OAuth 2.0 provider which authenticates user HTTP session in case of successful authentication
- implementation of authentication methods
  - the authentication process consists of one or more authentication methods which are invoked as defined by the Next Step Server
  - each authentication method is stored in a separate project
- central point which communicates with other backends
  - communication with Next Step Server to get a response about the next step and redirect of user to the right screen, lookup user, authenticate user, and deliver OTP authorization codes
  - communication with Data Adapter to perform TLS certificate verification, obtain consent options, validate consent data, save consent data, perform AFS actions, map complex operations, and notify Data Adapter about form data changes
  - communication with PowerAuth Server to obtain activation status, signature verification and token verification
  - communication with PowerAuth Push Server to deliver push messages to user mobile device
- Web Socket communication with frontend for immediate response in Mobile Token authentication
- tracking of operations within HTTP session to handle concurrent operations
- preparation of operation form data (value formatting, resource localization, resource translation)
- OAuth 2.0 consent form display, processing of consent options, and consent form validation
- authentication using SCA and non-SCA methods with credentials and/or OTP authorization codes
- authentication using client TLS certificate
