FROM eclipse-temurin:21-jre

LABEL maintainer="Wultra <hello@wultra.com>"

ENV APP_PATH="/app/powerauth-nextstep.war" \
    LB_HOME=/usr/local/liquibase \
    LB_VERSION=4.30.0

ENV PATH=$PATH:$LB_HOME

# Upgrade OS and dependencies
RUN apt-get -y update  \
    && apt-get -y upgrade \
    && apt-get -y install bash wget \
# Install Liquibase, inspired by https://github.com/mobtitude/liquibase/blob/master/Dockerfile
    && set -x \
    && wget -q -O /tmp/liquibase.tar.gz "https://github.com/liquibase/liquibase/releases/download/v$LB_VERSION/liquibase-$LB_VERSION.tar.gz" \
    && [ "184ffd609518091da42d6cd75e883b4f6ff1763cce8883e95fc99f7f05ca262d  /tmp/liquibase.tar.gz" = "$(sha256sum /tmp/liquibase.tar.gz)" ] \
    && mkdir -p "$LB_HOME" \
    && tar -xzf /tmp/liquibase.tar.gz -C "$LB_HOME" \
    && rm -rf "$LB_HOME/sdk" "$LB_HOME/examples" \
# Uninstall packages which are no longer needed and clean apt caches
    && apt-get -y remove wget gettext-base \
    && apt-get -y purge --auto-remove \
    && rm -rf /tmp/* /var/cache/apt/* \
# Remove default Liquibase data
    && rm -rf $LB_HOME/data \
# Add PowerAuth user
    && groupadd -r powerauth && useradd -r -g powerauth -s /sbin/nologin powerauth

# Copy new Liquibase data
COPY docs/db/changelog/changesets/powerauth-nextstep $LB_HOME/db/changelog

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
