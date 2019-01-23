# PowerAuth Web Flow

PowerAuth Web Flow provides federated authentication and authorization services for securing web applications. Web Flow is built using [PowerAuth security protocol](https://github.com/wultra/powerauth-crypto/wiki) and performs role of [an OAuth 2.0 provider](http://projects.spring.io/spring-security-oauth/docs/oauth2.html).

Typical use-case for the software stack is securing the RESTful API interfaces, or authentication / authorization for internet banking via central authentication / authorization component.

Web Flow handles the authentication and authorization from user point of view. The user interacts with Web Flow using web browser and using mobile device (optional). The authentication flow starts when user performs a request to a protected URL and the user session has not been authenticated yet. This event triggers an OAuth 2.0 authentication process where Web Flow serves as an OAuth 2.0 provider. Based on configuration of current operation, user gets authenticated using various authentication steps. User session becomes authenticated once all required steps have been successfully completed for given operation.

Web Flow can handle various types of authentication and authorization steps during the OAuth 2.0 dance:
* Form based authentication (login using username and password)
* User ID assignment (user identification is resolved using backend systems)
* SMS OTP authorization (user receives a SMS with one time code which is used for authorization)
* Mobile token authorization (user confirms operation on mobile device, PowerAuth signature is used for authorization)

Additional authentication and authorization steps can be implemented by extending Web Flow. Each operation can be configured to require a different authentication/authorization flow based on security requirements of the operation.
