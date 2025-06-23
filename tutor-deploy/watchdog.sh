#!/bin/bash

SERVICES=$(docker ps --format '{{.Names}}')

for service in $SERVICES; do
  health_status=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$service" 2>/dev/null)

  if [ "$health_status" = "none" ] || [ "$health_status" = "starting" ]; then
    continue
  fi

  if [ "$health_status" = "unhealthy" ]; then
    echo "[$(date)] Service '$service' is unhealthy. Restarting..."
    docker restart "$service"
  fi
done