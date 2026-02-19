FROM liquibase:4.33.0-alpine

LABEL maintainer="Wultra <hello@wultra.com>"

ENV MSSQL_JDBC_VERSION="13.2.1.jre11" \
    JDBC_DRIVERS_PATH="/liquibase/internal/lib"

COPY docs/db/changelog/changesets/powerauth-nextstep /liquibase/changelog
COPY powerauth-nextstep/docker-init/docker-entrypoint.sh /docker-entrypoint.sh

RUN rm ${JDBC_DRIVERS_PATH}/snowflake-jdbc.jar
RUN wget --no-verbose https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/${MSSQL_JDBC_VERSION}/mssql-jdbc-${MSSQL_JDBC_VERSION}.jar -O ${JDBC_DRIVERS_PATH}/mssql-jdbc.jar

USER liquibase
ENTRYPOINT ["/docker-entrypoint.sh"]

