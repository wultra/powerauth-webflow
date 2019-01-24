# Installation Manual for Tomcat

## Purpose of this document

This manual describes the installation of Web Flow including setting up the environment. The manual assumes installation on a Linux system, however installation on other OSes contains similar steps.

If you prefer a faster setup, consider trying our [Docker images](https://github.com/wultra/powerauth-docker).

### Create required user and group

Create group "tomcat" and user "tomcat":

`$ groupadd tomcat`

`$ useradd -s /bin/false -g tomcat -d /opt/tomcat tomcat`

`$ chmod 775 tomcat`

Optionally, add your user to the "tomcat" group:

`$ usermod -a -G tomcat ext_johndoe`


### Install Bouncy Castle

Please follow our [Bouncy Castle installation tutorial](https://github.com/wultra/powerauth-server/blob/develop/docs/Installing-Bouncy-Castle.md).

### Install Tomcat

Unzip Tomcat 8.5.14 to "/opt/tomcat" folder. You can download Tomcat here:

[https://tomcat.apache.org/download-80.cgi](https://tomcat.apache.org/download-80.cgi)

Change owner of the files to "tomcat" user:

`sudo chown -R tomcat:tomcat /opt/tomcat/`

### Customize application

Create folder "/opt/ext-resources".

Change owner of the "/opt/ext-resources" to "tomcat:tomcat":

`$ sudo chown -R tomcat:tomcat /opt/ext-resources/`

`$ sudo chmod -R 775 /opt/ext-resources/`

`$ sudo chmod -R -x+X /opt/ext-resources/`

Copy all resources which you want to modify into this folder. See resources in the [powerauth-webflow-customization project](https://github.com/wultra/powerauth-webflow-customization/tree/master/ext-resources) which contains original resources which can be modified for the concrete installation.

### Add required libraries

#### Oracle:
Copy "ojdbc6.jar" to "/opt/tomcat/lib" folder, so that the Oracle DB connector is on classpath. You can get the required JAR here:

https://mvnrepository.com/artifact/oracle/ojdbc6/11.2.0.3

#### MySQL:

Copy "mysql-connector-java-6.0.6.jar" to "/opt/tomtact/lib" folder, so that the MySQL DB connector is on classpath. You can get the required JAR here:

http://central.maven.org/maven2/mysql/mysql-connector-java/6.0.6

#### Other databases

Find the JDBC client driver for the database and install it using similar steps as the steps above.

### Fix address configurations on Tomcat

Edit "/opt/tomcat/conf/server.xml" so that the HTTP connector has the correct IP address:

`<Connector port="8080" protocol="HTTP/1.1" address="10.x.x.x" connectionTimeout="20000" redirectPort="8443" />`

### Create database schema - MySQL

* Create a new database or reuse an existing PowerAuth 2.0 database.
* Run the [create_schema.sql](./sql/mysql/create_schema.sql) script to create tables.
* Run the [initial_data.sql](./sql/mysql/initial_data.sql) script to load initial data.

For more details see document [Database Table Structure](./Database-Table-Structure.md).

### Create database schema - Oracle

* Create a new database or reuse an existing PowerAuth 2.0 database.
* Run the [create_schema.sql](./sql/oracle/create_schema.sql) script to create tables.
* Run the [initial_data.sql](./sql/oracle/initial_data.sql) script to load initial data.

For more details see document [Database Table Structure](./Database-Table-Structure.md).

### Update application configurations

Copy XML files described below to "/opt/tomcat/conf/Catalina/localhost". Then, update configurations in the files to reflect expected values. Make sure to use absolute URLs, not references to `localhost`, for example:

`<Parameter name="powerauth.credentials.service.url" value="http://10.x.x.x:8080/powerauth-credential-server-sample"/>`

`<Parameter name="powerauth.nextstep.service.url" value="http://10.x.x.x:8080/powerauth-nextstep"/>`

#### powerauth-webflow.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>

    <!-- PowerAuth 2.0 Credential Service URL -->
    <Parameter name="powerauth.credentials.service.url" value="http://10.x.x.x:8080/powerauth-credential-server-sample"/>

    <!-- PowerAuth 2.0 Next Step Service URL -->
    <Parameter name="powerauth.nextstep.service.url" value="http://10.x.x.x:8080/powerauth-nextstep"/>

    <!-- PowerAuth 2.0 Server Service URL -->
    <Parameter name="powerauth.service.url" value="http://10.x.x.x:8080/powerauth-java-server/soap"/>

    <!-- PowerAuth 2.0 Server Service Security Settings -->
    <Parameter name="powerauth.service.security.clientToken" value=""/>
    <Parameter name="powerauth.service.security.clientSecret" value=""/>
    <Parameter name="powerauth.service.ssl.acceptInvalidSslCertificate" value="false"/>

    <!-- PowerAuth 2.0 Push Server URL -->
    <Parameter name="powerauth.push.service.url" value="http://10.x.x.x:8080/powerauth-push-server"/>
    <Parameter name="powerauth.push.service.appId" value="2"/>

    <!-- PowerAuth 2.0 WebFlow Page Customization -->
    <Parameter name="powerauth.webflow.page.title" value="XYZ Bank - Web Authentication"/>
    <Parameter name="powerauth.webflow.page.ext-resources.location" value="file:/opt/ext-resources/"/>
    <Parameter name="powerauth.webflow.page.custom-css.url" value=""/>

    <!-- Database Configuration - JDBC -->
    <Parameter name="spring.datasource.url" value="jdbc:oracle:thin:@//hostname:1523/SID"/>
    <Parameter name="spring.datasource.username" value="powerauth"/>
    <Parameter name="spring.datasource.password" value="********"/>
    <Parameter name="spring.datasource.driver-class-name" value="oracle.jdbc.OracleDriver"/>
    <Parameter name="spring.jpa.hibernate.ddl-auto" value="none"/>
    <Parameter name="spring.jpa.properties.hibernate.default_schema" value="powerauth"/>

</Context>
```

#### powerauth-java-server.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>

    <!-- Database Configuration - JDBC -->
    <Parameter name="spring.datasource.url" value="jdbc:oracle:thin:@//hostname:1523/SID"/>
    <Parameter name="spring.datasource.username" value="powerauth"/>
    <Parameter name="spring.datasource.password" value="********"/>
    <Parameter name="spring.datasource.driver-class-name" value="oracle.jdbc.OracleDriver"/>
    <Parameter name="spring.jpa.hibernate.ddl-auto" value="none"/>
    <Parameter name="spring.jpa.properties.hibernate.default_schema" value="powerauth"/>

    <!-- Application Configuration -->
    <Parameter name="powerauth.service.applicationName" value="powerauth"/>
    <Parameter name="powerauth.service.applicationDisplayName" value="PowerAuth 2.0 Server"/>
    <Parameter name="powerauth.service.applicationEnvironment" value=""/>

    <!-- Security Configuration -->
    <Parameter name="powerauth.service.restrictAccess" value="false"/>

</Context>
```

#### powerauth-admin.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>

    <!-- PowerAuth 2.0 Server Service URL -->
    <Parameter name="powerauth.service.url" value="http://10.x.x.x:8080/powerauth-java-server/soap"/>

</Context>
```

#### powerauth-nextstep.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>

    <!-- Database Configuration - JDBC -->
    <Parameter name="spring.datasource.url" value="jdbc:oracle:thin:@//hostname:1523/SID"/>
    <Parameter name="spring.datasource.username" value="powerauth"/>
    <Parameter name="spring.datasource.password" value="********"/>
    <Parameter name="spring.datasource.driver-class-name" value="oracle.jdbc.OracleDriver"/>
    <Parameter name="spring.jpa.hibernate.ddl-auto" value="none"/>
    <Parameter name="spring.jpa.properties.hibernate.default_schema" value="powerauth"/>

</Context>
```

#### powerauth-push-server.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>

    <!-- PowerAuth 2.0 Server Service URL -->
    <Parameter name="powerauth.service.url" value="http://10.x.x.x:8080/powerauth-java-server/soap"/>

    <!-- PowerAuth 2.0 Server Service Security Settings -->
    <Parameter name="powerauth.service.security.clientToken" value=""/>
    <Parameter name="powerauth.service.security.clientSecret" value=""/>
    <Parameter name="powerauth.service.ssl.acceptInvalidSslCertificate" value="false"/>

    <!-- Database Configuration - JDBC -->
    <Parameter name="spring.datasource.url" value="jdbc:oracle:thin:@//hostname:1523/SID"/>
    <Parameter name="spring.datasource.username" value="powerauth"/>
    <Parameter name="spring.datasource.password" value="********"/>
    <Parameter name="spring.datasource.driver-class-name" value="oracle.jdbc.OracleDriver"/>
    <Parameter name="spring.jpa.hibernate.ddl-auto" value="none"/>
    <Parameter name="spring.jpa.properties.hibernate.default_schema" value="powerauth"/>

    <!-- APNS Configuration -->
    <Parameter name="powerauth.push.service.apns.useDevelopment" value="true"/>

</Context>
```

#### powerauth-webflow-client.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>

    <!-- PowerAuth 2.0 OAuth 2.0 API Config -->
    <Parameter name="powerauth.webflow.service.url" value="http://10.x.x.x:8080/powerauth-webflow"/>
    <Parameter name="powerauth.webflow.service.oauth.authorizeUrl" value="http://10.x.x.x:8080/powerauth-webflow/oauth/authorize"/>
    <Parameter name="powerauth.webflow.service.oauth.tokenUrl" value="http://10.x.x.x:8080/powerauth-webflow/oauth/token"/>
    <Parameter name="powerauth.webflow.service.oauth.clientId" value="foo"/>
    <Parameter name="powerauth.webflow.service.oauth.clientSecret" value="bar"/>

    <!-- PowerAuth 2.0 Next Step Config -->
    <Parameter name="powerauth.nextstep.service.url" value="http://10.x.x.x:8080/powerauth-nextstep"/>

    <!-- Database Configuration - JDBC -->
    <Parameter name="spring.datasource.url" value="jdbc:oracle:thin:@//hostname:1523/SID"/>
    <Parameter name="spring.datasource.username" value="powerauth"/>
    <Parameter name="spring.datasource.password" value="********"/>
    <Parameter name="spring.datasource.driver-class-name" value="oracle.jdbc.OracleDriver"/>
    <Parameter name="spring.jpa.hibernate.ddl-auto" value="none"/>
    <Parameter name="spring.jpa.properties.hibernate.default_schema" value="powerauth"/>

</Context>
```

### Copy applications

Create war artifacts using steps described in [Compilation, Packaging and Deployment](./Compilation,-Packaging-and-Deployment.md) and copy them into /opt/tomcat/webapps.

### Configure Web Flow

Web Flow needs to be configured before starting. See chapter [Web Flow Configuration](./Web-Flow-Configuration.md).

### Starting Tomcat

Start Tomcat service as the "tomcat" user:

`$ sudo -u tomcat sh /opt/tomcat/bin/catalina.sh start`

### Launching Tomcat in debug mode

Start Tomcat with following command:

`$ JPDA_OPTS="-agentlib:jdwp=transport=dt_socket,address=9002,server=y,suspend=n" sh /opt/tomcat/bin/catalina.sh jpda start`

### Observing Tomcat logs

To observe tomcat logs interactively, use following command:

`$ tail -f -n200 /opt/tomcat/logs/catalina.out`


