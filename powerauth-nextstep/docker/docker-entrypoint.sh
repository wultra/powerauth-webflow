#!/usr/bin/env bash
set -euo pipefail

exec java -Dserver.port=8080 -Dspring.config.additional-location=/app/application.properties ${JAVA_OPTS:-} -cp "${APP_PATH}:/app/extlib/*" org.springframework.boot.loader.launch.WarLauncher
