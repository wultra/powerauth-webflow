# Data Adapter

Data Adapter integrates Web Flow with other backends required for the authentication process.

You can obtain the war file which can be deployed to a Java EE container in [releases](https://github.com/wultra/powerauth-webflow/releases).

The Data Adapter needs to be customized for each deployment. You can find the documentation for customizing Data Adapter in the [Web Flow Customization project](https://github.com/wultra/powerauth-webflow-customization/blob/develop/docs/Implementing-the-Data-Adapter-Interface.md).

## Data Adapter functionality

Following functionality needs to be customized during Web Flow deployment by implementing the Data Adapter interface (if applicable):

- convert username to user ID in case such conversion is required
- perform user authentication against remote backend based on provided credentials
- retrieve user details for given user ID
- initialize an authentication method and set its parameters, e.g. client certificate configuration
- decorate form data for given user (e.g. add user bank account list)
- form data change notification
- create an implicit login operation automatically on authentication start
- map a complex operation into smaller operations and configure PowerAuth operation template
- operation status change notification
- generate OTP authorization code and send authorization SMS
- send authorization SMS with previously generated OTP authorization code   
- verify OTP authorization code from SMS
- authenticate user using user ID, password and OTP authorization code
- verify a client TLS certificate 
- initialize OAuth 2.0 consent form
- create OAuth 2.0 consent form
- validate OAuth 2.0 consent form options
- save OAuth 2.0 consent form options
- execute an anti-fraud system (AFS) action and react on response from AFS