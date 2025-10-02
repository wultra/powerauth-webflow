FROM liquibase:4.31.1-alpine

LABEL maintainer="Wultra <hello@wultra.com>"

COPY docs/db/changelog/changesets/powerauth-nextstep /liquibase/changelog
COPY powerauth-nextstep/docker-init/docker-entrypoint.sh /

USER liquibase
ENTRYPOINT ["/docker-entrypoint.sh"]

