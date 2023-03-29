# Developer - How to Start Guide


## Webflow


### Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/PowerAuthWebFlowApplication.run.xml`
- Open [http://localhost:9080/powerauth-webflow/actuator/health](http://localhost:9080/powerauth-webflow/actuator/health) and you should get `{"status":"UP"}`


### Database

Database changes are driven by Liquibase.

This is an example how to manually check the Liquibase status.
Important and fixed parameter is `changelog-file`.
Others (like URL, username, password) depend on your environment.

```shell
liquibase --changelog-file=./docs/db/changelog/changesets/powerauth-webflow/db.changelog-module.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth --hub-mode=off status
```


## TPP Engine


### Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/TppEngineApplication.run.xml`
- Open [http://localhost:9081/tpp-engine/actuator/health](http://localhost:9081/tpp-engine/actuator/health) and you should get `{"status":"UP"}`


### Database

Database changes are driven by Liquibase.

This is an example how to manually check the Liquibase status.
Important and fixed parameter is `changelog-file`.
Others (like URL, username, password) depend on your environment.

```shell
liquibase --changelog-file=./docs/db/changelog/changesets/powerauth-tpp-engine/db.changelog-module.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth --hub-mode=off status
```


## NextStep


### Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/NextStepApplication.run.xml`
- Open [http://localhost:9082/powerauth-nextstep/actuator/health](http://localhost:9082/powerauth-nextstep/actuator/health) and you should get `{"status":"UP"}`


### Database

Database changes are driven by Liquibase.

This is an example how to manually check the Liquibase status.
Important and fixed parameter is `changelog-file`.
Others (like URL, username, password) depend on your environment.

```shell
liquibase --changelog-file=./docs/db/changelog/changesets/powerauth-nextstep/db.changelog-module.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth --hub-mode=off status
```

When all user identities are not stored in Next Step, avoid adding foreign keys for user identity by using CLI option `--contexts=exclude-user-identity`.

## Webflow Client


### Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/PowerAuthWebFlowClientApplication.run.xml`
- Open [http://localhost:9083/powerauth-webflow-client/actuator/health](http://localhost:9083/powerauth-webflow-client/actuator/health) and you should get `{"status":"UP"}`
