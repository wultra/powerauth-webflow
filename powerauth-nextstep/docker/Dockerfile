FROM ibm-semeru-runtimes:open-21.0.9_10-jre-noble

LABEL maintainer="Wultra <hello@wultra.com>"

ENV APP_PATH="/app/powerauth-nextstep.war" \
    EXTLIB_PATH="/app/extlib" \
    OJDBC_VERSION="23.8.0.25.04" \
    MSSQL_JDBC_VERSION="13.2.1.jre11"

# Add PowerAuth user
RUN groupadd -r powerauth && useradd -r -g powerauth -s /sbin/nologin powerauth

RUN mkdir -p ${EXTLIB_PATH} \
    && wget --no-verbose https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/${OJDBC_VERSION}/ojdbc11-${OJDBC_VERSION}.jar -O ${EXTLIB_PATH}/ojdbc11.jar \
    && wget --no-verbose https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/${MSSQL_JDBC_VERSION}/mssql-jdbc-${MSSQL_JDBC_VERSION}.jar -O ${EXTLIB_PATH}/mssql-jdbc.jar

# Deploy and run applications
COPY powerauth-nextstep/target/powerauth-nextstep-*.war /app/powerauth-nextstep.war

# Docker configuration
HEALTHCHECK --interval=10s --timeout=3s --retries=1 \
  CMD curl --fail --silent http://localhost:8080/actuator/health | grep UP || exit 1
EXPOSE 8080

USER powerauth

COPY powerauth-nextstep/docker/docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
