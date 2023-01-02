# Developer - How to Start Guide


## Webflow


### Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/PowerAuthWebFlowApplication.run.xml`
- Open [http://localhost:9080/powerauth-webflow/actuator/health](http://localhost:9080/powerauth-webflow/actuator/health) and you should get `{"status":"UP"}`


## TPP Engine


### Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/TppEngineApplication.run.xml`
- Open [http://localhost:9081/tpp-engine/actuator/health](http://localhost:9081/tpp-engine/actuator/health) and you should get `{"status":"UP"}`


## NextStep


### Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/NextStepApplication.run.xml`
- Open [http://localhost:9082/powerauth-nextstep/actuator/health](http://localhost:9082/powerauth-nextstep/actuator/health) and you should get `{"status":"UP"}`


## Webflow Client


### Standalone Run

- Enable maven profile `standalone`
- Use IntelliJ Idea run configuration at `../.run/PowerAuthWebFlowClientApplication.run.xml`
- Open [http://localhost:9083/powerauth-webflow-client/actuator/health](http://localhost:9083/powerauth-webflow-client/actuator/health) and you should get `{"status":"UP"}`
