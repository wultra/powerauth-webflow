FROM liquibase:4.33.0-alpine

LABEL maintainer="Wultra <hello@wultra.com>"

COPY docs/db/changelog/changesets/powerauth-nextstep /liquibase/changelog
COPY powerauth-nextstep/docker-init/docker-entrypoint.sh /

USER liquibase
ENTRYPOINT ["/docker-entrypoint.sh"]

