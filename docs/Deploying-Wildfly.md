# Deploying Web Flow on JBoss / Wildfly

Two modules need to be configured for JBoss / Wildfly:
- Web Flow - the main Web Flow application
- Next Step - a backend service for Web Flow which manages operation steps and authentication methods

Optionally Web Flow Client application and TPP Engine application can be also configured. 

## JBoss Deployment Descriptor 

Web Flow contains the following configuration in `jboss-deployment-structure.xml` file for JBoss / Wildfly:

```
<?xml version="1.0"?>
<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
	<deployment>
		<exclude-subsystems>
			<!-- disable the logging subsystem because the application manages its own logging independently -->
			<subsystem name="logging" />
		</exclude-subsystems>

		<dependencies>
			<module name="com.wultra.powerauth.webflow.conf" />
		</dependencies>
		
		<resources>
			<!-- use WAR provided Bouncy Castle -->
			<resource-root path="WEB-INF/lib/bcprov-jdk18on-1.72.jar" use-physical-code-source="true"/>
		</resources>

		<local-last value="true" />
	</deployment>
</jboss-deployment-structure>
```

Similarly, Next Step contains the following configuration in `jboss-deployment-structure.xml` file for JBoss / Wildfly:
```
<?xml version="1.0"?>
<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
	<deployment>
		<exclude-subsystems>
			<!-- disable the logging subsystem because the application manages its own logging independently -->
			<subsystem name="logging" />
		</exclude-subsystems>

		<dependencies>
			<module name="com.wultra.powerauth.nextstep.conf" />
		</dependencies>
		
		<resources>
			<!-- use WAR provided Bouncy Castle -->
			<resource-root path="WEB-INF/lib/bcprov-jdk18on-1.72.jar" use-physical-code-source="true"/>
		</resources>

		<local-last value="true" />
	</deployment>
</jboss-deployment-structure>
```

Optionally, TPP engine contains the following configuration in `jboss-deployment-structure.xml` file for JBoss / Wildfly:
```
<?xml version="1.0"?>
<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
	<deployment>
		<exclude-subsystems>
			<!-- disable the logging subsystem because the application manages its own logging independently -->
			<subsystem name="logging" />
		</exclude-subsystems>

		<dependencies>
			<module name="com.wultra.powerauth.tpp-engine.conf" />
		</dependencies>
		<local-last value="true" />
	</deployment>
</jboss-deployment-structure>
```

Optionally, Web Flow Client contains the following configuration in `jboss-deployment-structure.xml` file for JBoss / Wildfly:

```
<?xml version="1.0"?>
<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
	<deployment>
		<exclude-subsystems>
			<!-- disable the resource-adapters subsystem to prevent the application's HSQLDB driver
			from being included in application server JDBC drivers -->
			<subsystem name="resource-adapters" />
			<!-- disable the logging subsystem because the application manages its own logging independently -->
			<subsystem name="logging" />
		</exclude-subsystems>
		<dependencies>
			<module name="com.wultra.powerauth.webflow-client.conf" />
		</dependencies>
		<local-last value="true" />
	</deployment>
</jboss-deployment-structure>
```

The deployment descriptor requires configuration of the `com.wultra.powerauth.webflow.conf` and `com.wultra.powerauth.nextstep.conf` modules.
Optionally configure also the `com.wultra.powerauth.tpp-engine.conf` and `com.wultra.powerauth.webflow-client.conf` modules.

## JBoss Module for Web Flow Configuration

Create a new module in `PATH_TO_JBOSS/modules/system/layers/base/com/wultra/powerauth/webflow/conf/main` and `PATH_TO_JBOSS/modules/system/layers/base/com/wultra/powerauth/nextstep/conf/main` .

The files described below should be added into this folder.

### Main Module Configuration

The `module.xml` configuration is used for module registration. It also adds resources from the module folder to classpath.

Web Flow module configuration:
```
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.3" name="com.wultra.powerauth.webflow.conf">
    <resources>
        <resource-root path="." />
    </resources>
</module>
```

Web Flow Client module configuration:
```
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.3" name="com.wultra.powerauth.webflow-client.conf">
    <resources>
        <resource-root path="." />
    </resources>
</module>
```

Next Step module configuration: 
```
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.3" name="com.wultra.powerauth.nextstep.conf">
    <resources>
        <resource-root path="." />
    </resources>
</module>
```

### Logging Configuration

Use the `logback.xml` file to configure logging, for example:
```
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">

        <property name="LOG_FILE_DIR" value="/var/log/powerauth" />
        <property name="LOG_FILE_NAME" value="webflow" />
        <property name="INSTANCE_ID" value="${jboss.server.name}" />

        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
                <file>${LOG_FILE_DIR}/${LOG_FILE_NAME}-${INSTANCE_ID}.log</file>
                <immediateFlush>true</immediateFlush>
                <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
                        <fileNamePattern>${LOG_FILE_DIR}/${LOG_FILE_NAME}-${INSTANCE_ID}-%d{yyyy-MM-dd}-%i.log</fileNamePattern>
                        <maxFileSize>10MB</maxFileSize>
                        <maxHistory>5</maxHistory>
                        <totalSizeCap>100MB</totalSizeCap>
                </rollingPolicy>
                <encoder>
                        <charset>UTF-8</charset>
                        <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
                </encoder>
        </appender>

        <logger name="com.wultra" level="INFO" />
        <logger name="io.getlime" level="INFO" />

        <root level="INFO">
                <appender-ref ref="FILE" />
        </root>
</configuration>
```

For Next Step you can use the same configuration, just change the variable `LOG_FILE_NAME`.

### Application Configuration

The `application-ext.properties` file is used to override default configuration properties, for example:
```
# PowerAuth 2.0 Client configuration
powerauth.service.url=http://[host]:[port]/powerauth-java-server/rest
```

Web Flow Spring application uses the `ext` Spring profile which activates overriding of default properties by `application-ext.properties`.

You need to configure separate `application-ext.properties` files for Web Flow and Next Step in each module. 

### Bouncy Castle Installation

The Bouncy Castle library for JBoss / Wildfly is included in the Web Flow and Next Step war files. The library is configured 
using the `jboss-deployment-structure.xml` descriptor. Global module configuration of Bouncy Castle is no longer required.