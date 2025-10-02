#!/usr/bin/env bash
set -euo pipefail

liquibase --headless=true --log-level=INFO --changeLogFile="${LB_HOME}/db/changelog/db.changelog-module.xml" \
  --username="${NEXTSTEP_DATASOURCE_USERNAME}" \
  --password="${NEXTSTEP_DATASOURCE_PASSWORD}" \
  --url="${NEXTSTEP_DATASOURCE_URL}" \
  update

exec java -Dserver.port=8080 -Dspring.config.additional-location=/app/application.properties ${JAVA_OPTS:-} -jar "${APP_PATH}"
