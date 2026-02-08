#!/usr/bin/env bash
set -euo pipefail

exec java -Dserver.port=8080 ${JAVA_OPTS:-} -Dserver.servlet.context-path=/powerauth-nextstep -cp "${APP_PATH}:/app/extlib/*" org.springframework.boot.loader.launch.WarLauncher
