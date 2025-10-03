FROM eclipse-temurin:21-jre

LABEL maintainer="Wultra <hello@wultra.com>"

ENV APP_PATH="/app/powerauth-nextstep.war" \
    EXTLIB_PATH="/app/extlib" \
    OJDBC_VERSION="23.8.0.25.04"

# Add PowerAuth user
RUN groupadd -r powerauth && useradd -r -g powerauth -s /sbin/nologin powerauth

RUN mkdir -p ${EXTLIB_PATH} && wget --no-verbose https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/${OJDBC_VERSION}/ojdbc11-${OJDBC_VERSION}.jar -O ${EXTLIB_PATH}/ojdbc11.jar
# Deploy and run applications
COPY powerauth-nextstep/docker/application.properties /app/application.properties
COPY powerauth-nextstep/target/powerauth-nextstep-*.war /app/powerauth-nextstep.war

# Docker configuration
HEALTHCHECK --interval=10s --timeout=3s --retries=1 \
  CMD curl --fail --silent http://localhost:8080/actuator/health | grep UP || exit 1
EXPOSE 8080

USER powerauth

COPY powerauth-nextstep/docker/docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
