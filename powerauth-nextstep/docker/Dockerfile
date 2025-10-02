FROM eclipse-temurin:21-jre

LABEL maintainer="Wultra <hello@wultra.com>"

ENV APP_PATH="/app/powerauth-nextstep.war"

# Add PowerAuth user
RUN groupadd -r powerauth && useradd -r -g powerauth -s /sbin/nologin powerauth

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
