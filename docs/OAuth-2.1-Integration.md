# OAuth 2.1 Integration

Web Flow serves a role of an [OAuth 2.1 provider](https://oauth.net/2.1).

The OAuth 2.1 standard defines following components:
- `OAuth 2.1 Authorization Server` - a server providing authorization services
- `OAuth 2.1 Resource Server` - a server providing access to resources
- `OAuth 2.1 Client` - a client using the authorization services

## OAuth 2.1 Authorization Server

Web Flow implements the `authorization code grant` type from the OAuth 2.1 specification. No other grant types are supported. The Authorization Server is provided by the [powerauth-webflow project](../powerauth-webflow).

Web Flow provides following endpoints:
- `/oauth2/authorize` - main OAuth 2.1 endpoint which triggers processing of a Web Flow operation
- `/oauth2/token` - provides the OAuth 2.1 access token
- `/oauth2/revoke` - revocation of the OAuth 2.1 access token
- `/oauth2/introspect` - introspection of the OAuth 2.1 access token

## OAuth 2.1 Resource Server

Web Flow implements a basis Resource Server which provides access to the user profile. The Resource Server is provided by the [powerauth-webflow-resources project](../powerauth-webflow-resources).

Web Flow Resource Server provides following endpoints:
- `/api/secure/profile/me` - provides information about authenticated user (user ID, firstName, familyName), connection (language, SCA) and service (application name, environment, timestamp)
- `/api/secure/profile/me/info` - provides user information for OpenID Connect (OIDC)

## OAuth 2.1 Client

A sample OAuth 2.1 client is implemented in the [powerauth-webflow-client project](../powerauth-webflow-client). It is expected that during Web Flow deployment a similar client will be created for preparing data and form data related to the operation.

The sample code for creating an operation and customizing operation form data is available in the [Customizing Operation Form Data chapter](./Customizing-Operation-Form-Data.md).

## OAuth 2.1 Consent Screen

The [Data Adapter project](https://github.com/wultra/powerauth-webflow-customization) can be used to customize the OAuth 2.1 screen with custom form and options.
The consent form is shown in Web Flow when the `CONSENT` method is included in operation steps.
 