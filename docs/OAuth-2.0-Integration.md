# OAuth 2.0 Integration

Web Flow serves a role of an [OAuth 2.0 provider](http://projects.spring.io/spring-security-oauth/docs/oauth2.html).

The OAuth 2.0 standard defines following components:
- `OAuth 2.0 Authorization Server` - a server providing authorization services
- `OAuth 2.0 Resource Server` - a server providing access to resources
- `OAuth 2.0 Client` - a client using the authorization services

## OAuth 2.0 Authorization Server

Web Flow implements the `authorization code grant` type from the OAuth 2.0 specification. No other grant types are supported. The Authorization Server is provided by the [powerauth-webflow project](../powerauth-webflow).

Web Flow provides following endpoints:
- `/oauth/authorize` - main OAuth2.0 endpoint which triggers processing of a Web Flow operation
- `/oauth/token` - provides access to the OAuth 2.0 token

Web Flow only uses OAuth 2.0 access tokens and does not issue refresh tokens.

## OAuth 2.0 Resource Server

Web Flow implements a basis Resource Server which provides access to the user profile. The Resource Server is provided by the [powerauth-webflow-resources project](../powerauth-webflow-resources).

Web Flow Resource Serverr provides following endpoints:
- `/api/secure/profile/me` - provides information about authenticated user (user ID, firstName, familyName), connection (language, SCA) and service (application name, environment, timestamp)

## OAuth 2.0 Client

A sample OAuth 2.0 client is implemented in the [powerauth-webflow-client project](../powerauth-webflow-client). It is expected that during Web Flow deployment a similar client will be created for preparing data and form data related to the operation.

The sample code for creating an operation and customizing operation form data is available in the [Customizing Operation Form Data chapter](./Customizing-Operation-Form-Data.md).
