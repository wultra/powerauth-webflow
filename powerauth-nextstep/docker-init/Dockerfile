FROM liquibase:4.33.0-alpine

LABEL maintainer="Wultra <hello@wultra.com>"

COPY docs/db/changelog/changesets/powerauth-nextstep /liquibase/changelog
COPY powerauth-nextstep/docker-init/docker-entrypoint.sh /docker-entrypoint.sh

RUN rm /liquibase/internal/lib/snowflake-jdbc.jar

USER liquibase
ENTRYPOINT ["/docker-entrypoint.sh"]

