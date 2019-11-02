# Compilation, Packaging and Deployment

Web Flow uses Maven for compilation and packaging. Java 8 should be used to compile Web Flow.

Web Flow can be deployed to any Java web container (such as Tomcat) using war artifacts produced during compilation.

## Dependencies

In order to build Web Flow using Maven, following PowerAuth dependencies need to be satisfied:

* [powerauth-server](https://github.com/wultra/powerauth-server) - dependency **powerauth-java-client-spring**
* [powerauth-push-server](https://github.com/wultra/powerauth-push-server) - dependency **powerauth-push-client**
* [powerauth-restful-integration](https://github.com/wultra/powerauth-restful-integration) - dependency **powerauth-restful-security-spring**
* [powerauth-crypto](https://github.com/wultra/powerauth-crypto) - dependency **powerauth-java-crypto**
* [lime-java-core](https://github.com/wultra/lime-java-core) - dependency **rest-model-base**

These projects should be installed into local Maven repository using the "install" goal before building Web Flow. This step may not be necessary in case these packages are already published in the public repository in their required versions.

Other dependencies are resolved automatically by Maven by downloading packages from public Maven repositories.

## Building Web Flow

Web Flow can be built using [the parent pom.xml file](../pom.xml) of project [powerauth-webflow](https://github.com/wultra/powerauth-webflow#docucheck-keep-link). Use the "package" goal to generate the war artifacts. The war files will be built in target subfolders of project folders. In case the compilation fails, check for missing PowerAuth dependencies to be installed.

## Deployment

Once you create a war package using steps above and set up database for Web Flow, you can deploy the war files to any Java web container, such as Tomcat or any Java EE server. You can remove the version from the war file for a nicer target URL in the container (e.g. rename powerauth-webflow-0.0.xx-SNAPSHOT.war to powerauth-webflow.war). After deployment, the client application frontend should be available at http[s]://host:port/powerauth-webflow-client.

The following war files need to be deployed for a fully functional demo of Web Flow:
* powerauth-webflow.war - the main Web Flow application
* powerauth-nextstep.war - the Next Step service
* powerauth-data-adapter.war - a Data Adapter for communication with client backends
* powerauth-webflow-client.war - demo client application

The whole installation process is described in the [Web Flow Installation Manual](./Web-Flow-Installation-Manual.md).

## Testing Web Flow

You can test the web flow demo application by navigating to: http://localhost:8080/powerauth-webflow-client

* Use the "Login" action to test the user authentication. The Credential Server Sample project uses "test" as password for any username.
* Use the "Payment (DEMO)" action to test payment authorization. You will need to enable POWERAUTH_TOKEN authentication method for the user who will authorize the payment (using [Next Step REST API](./Next-Step-Server-REST-API-Reference.md#enable-an-authentication-method-for-given-user)).
* Use the "Authorization" action to test operation authorization. In order to test this action you will need to create an operation and obtain its operationId (using [Next Step REST API](./Next-Step-Server-REST-API-Reference.md#create-an-operation)).

## Maven Profiles (Advanced)

There are following Maven profiles defined:
* **prod** - used for deployment to a production environment (default profile)
* **fast** - used for fast redeployment to a development environment (do not use when dependencies change, in this case prod build is required)
* **dev** - used for development in the IDE (mainly for debugging)

Path to the pom.xml file:

`powerauth-webflow/powerauth-webflow/pom.xml`

## Development

During development you can start the backend and the frontend separately for easier continuous redeployment.

### Backend
To start the backend part in the IDE, simply point the IDE to run Main class:

`io.getlime.security.powerauth.app.webflow.PowerAuthWebFlowApplication`

You should see a Spring boot console in IDE log and the last message should start with "Started PowerAuthWebFlowApplication". To redeploy, trigger a build in the IDE and Maven should redeploy changes automatically.

### Frontend
To start the frontend part in the IDE, use the **package** phase with the "dev" Maven profile:

`mvn package -P dev`

Maven builds the application and stops the deployment in the moment when webpack starts watching for changes. When you make any change in JavaScript code, you should see a message from compiler and bundle.js in target folder should be redeployed automatically using the WebpackDeployPlugin (see [webpack.config.js](../powerauth-webflow/webpack.config.js) and [webpack-deploy.js](../powerauth-webflow/src/main/js/webpack-deploy.js)). Note that bundle.js is built in full debug mode, hence the large size of the output of the compiler. Compiled frontend code is located in target/classes/static/built/bundle.js
