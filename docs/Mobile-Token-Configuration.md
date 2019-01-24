# Mobile Token Configuration

Mobile Token application requires configuration in order to set an active activation for the `POWERAUTH_TOKEN` authentication method for given user. Mobile token configuration should be performed when user activates the mobile token (e.g. in internet banking). When the user disables the mobile token, the activation should be unconfigured, too.

Web Flow uses the configured activation when sending push notifications and when authorizing operations using Mobile Token. When activation is not configured for the user, the `POWERAUTH_TOKEN` authentication method is not available and the user needs to use an alternative method for authorization of the operation (e.g. `SMS_KEY`).

See the [Next Step REST API documentation](./Next-Step-Server-REST-API-Reference.md#enable-an-authentication-method-for-given-user) which describes this configuration step.
