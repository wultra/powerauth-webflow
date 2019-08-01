# Web Flow Components

Web Flow consists of following compoments:
- [Web Flow Server](#web-flow-server)
- [Next Step Server](#next-step-server)
- [Data Adapter](#data-adapter)
- [Mobile Token](#mobile-token)
- [PowerAuth Server](#powerauth-server)
- [PowerAuth Push Server](#powerauth-push-server)
- [PowerAuth Admin](#powerauth-admin)

This chapter provides an introduction to each of these components and describes the role they serve in the authentication and authorization process.

To get an overview of how these compoments are related and where they are deployed in the network infrastructure, see the [Web Flow Architecture diagram](./Web-Flow-Architecture.md).

We will refer to the "authentication and authorization process" as "authentication process" for simplification.

## Web Flow Server

Web Flow Server is the central point which handles the authentication process.

Web Flow Server consists of following parts:
- **Frontend application** - a web application the user interacts with during the authentication.
  - This application is written in ReactJS and it communicates with the backend using [REST API](./Web-Flow-REST-API-Reference.md) and [Web Sockets](./Web-Socket-Communication-Protocol.md).
- **Backend services** - REST services which respond to requests from the frontend application and communicate with other components.
  - The logic of resolving next step in the operation is handled by the Next Step server, so the Web Flow Backend offloads all such decisions to the Next Step server. Handling of operation updates is done by Next Step, too.
  - Data Adapter is used to retrieve data from remote backends such as information about the user as well as trigger actions such as sending the authorization SMS with OTP.
  - Mobile Token interacts with Web Flow backend services to obtain information about current operation (retrieved from Next Step Server), signature verification (processed through PowerAuth Server) and push message delivery (requests sent to PowerAuth Push Server).

## Next Step Server

Next Step Server stores information about operations and resolves the next step for each operation. Various operations with different authentication flows can be configured in Next Step Server.

During the authentication process following communication occurs between Web Flow Server and Next Step Server:
- **Operation is created** - Web Flow Server sends a request to Next Step Server to create an operation and decide its next step. Next Step Server decides the next step of the operation based on its configuration for current operation.
- **Operation is updated** - user interacts with Web Flow (e.g. submits the login form) and Web Flow Server sends a request to update the existing operation with result of the current step. Next Step server decides the next step based of the operation based on its configuration for current operation and the result of the authentication step.

Based on Next Step response either of the following actions happens in Web Flow:
- a new page is shown to user representing a new authentication step
- same page is shown to the user with an error (the user didn't make any progress in operation)
- the authentication process is completed with a redirect
- an error is shown followed by a redirect with error details

## Data Adapter

Data Adapter connects Web Flow to other backends and serves as an integration component.

Data Adapter handles following use cases:

* Lookup user ID for given username.
* User authentication with remote backend based on provided credentials for form based authentication step.
* Retrieve user details for given user ID such as firstname and surname.
* Retrieve data for given user and decorate operation data (e.g. bank account names, balances, currencies, etc.).
* Notify backend about form data changes, e.g. when user fills in some data in Web Flow frontend.
* Notify backend about operation status changes: finished operation, failed operation and canceled operation.
* Send authorization SMS messages with OTP code -- message text is prepared and localized, however SMS message needs to be sent by the remote backend.
* Verify authorization SMS code specified by the user.
* Decide whether consent step should be displayed for given operation context.
* Prepare consent form text and options. 
* Verify consent form options selected by the user.
* Save consent form options selected by the user.
* Verify authorization SMS code and user password.

For more information see the [Web Flow customization project](https://github.com/wultra/powerauth-webflow-customization)

## Mobile Token

Mobile Token is a standalone mobile application which allows user to confirm authentication, or authorization operations, created on Next Step Server. Operations are typically accessed via REST API published by Web Flow Server.

For more information see our [Mobile Token product page](https://www.wultra.com/product/powerauth-mobile-token).

## PowerAuth Server

PowerAuth Server is used as a backend in authorization steps which require signature verification, token verification or access to the secure vault. These services are required by the Mobile Token application. PowerAuth Server is deployed in internal network and does not have a public facing API, for this reason all requests to PowerAuth Server are handled by Web Flow Server.

For more information see [the PowerAuth Server project](https://github.com/wultra/powerauth-server).

## PowerAuth Push Server

PowerAuth Push Server is used as a backend in authorization steps which require sending of push messages to user mobile devices (iOS or Android). These services are required by the Mobile Token application. PowerAuth Server is deployed in internal network and does not have a public facing API, for this reason all requests to PowerAuth Server are handled by Web Flow Server.

For more information see [the PowerAuth Push Server project](https://github.com/wultra/powerauth-push-server).

## PowerAuth Admin

PowerAuth Admin is used to administer PowerAuth Server. It is used to configure applications and application versions for PowerAuth server during deployment of Web Flow. PowerAuth Admin is also used for managing activations and for review of PowerAuth signatures.

For more information see [the PowerAuth Admin project](https://github.com/wultra/powerauth-admin).
