# Data Adapter

Data Adapter integrates Web Flow with other backends required for the authentication process.

You can obtain the war file which can be deployed to a Java EE container in [releases](https://github.com/wultra/powerauth-webflow/releases).

The Data Adapter needs to be customized for each deployment. You can find the documentation for customizing Data Adapter in the [Web Flow Customization project](https://github.com/wultra/powerauth-webflow-customization/blob/develop/docs/Implementing-the-Data-Adapter-Interface.md).

## Data Adapter functionality

Following functionality needs to be customized during Web Flow deployment by implementing the Data Adapter interface (if applicable):

- convert username to user ID in case such conversion is required
- perform user authentication with remote backend based on provided credentials
- retrieve user details for given user ID
- decorate form data for given user (e.g. add user bank account list)
- form data change notification
- operation status change notification
- send authorization SMS with generate text and authorization code 
- verify authorization code from SMS
- initialize OAuth 2.0 consent form
- create OAuth 2.0 consent form
- validate OAuth 2.0 consent form options
- save OAuth 2.0 consent form options
- authenticate user using user ID, password and SMS authorization code
