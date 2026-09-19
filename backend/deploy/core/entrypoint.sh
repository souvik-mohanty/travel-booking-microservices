#!/bin/sh
# Sequences the 5 hot-path JVMs (identity, catalog, booking, payment, then
# the gateway) inside ONE Render free instance (512MB RAM / 0.1 vCPU total,
# shared across all 5). Only the gateway's port is exposed externally --
# Render routes to whatever $PORT the container's foreground process binds,
# and every other service here is reached over localhost, the same way the
# gateway already reaches them locally (see application.yml's routes).
#
# Starting them one at a time and waiting for each to report healthy (rather
# than backgrounding all 5 immediately) avoids 5 JVMs class-loading/JIT-ing
# simultaneously on 0.1 vCPU, which is the difference between a ~60s boot and
# one that times out Render's own health check entirely.
#
# Heap sizes below are deliberately tight and were sized by estimate, not by
# measuring actual RSS on a real Render instance -- if any service gets
# OOM-killed in practice, that's the first place to look, and the real fix
# at that point is Render's cheapest paid tier (more RAM), not smaller heaps
# than this.
set -e

GATEWAY_PORT="${PORT:-8080}"
JVM_COMMON="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -XX:MaxMetaspaceSize=96m"

wait_healthy() {
  name="$1"
  port="$2"
  i=0
  while [ "$i" -lt 60 ]; do
    if curl -sf "http://localhost:${port}/actuator/health" >/dev/null 2>&1; then
      echo "[core] $name is healthy"
      return 0
    fi
    i=$((i + 1))
    sleep 2
  done
  echo "[core] $name did not become healthy within 120s" >&2
  return 1
}

echo "[core] starting identity-service"
java $JVM_COMMON -Xmx110m -jar identity-service.jar --server.port=8081 --server.tomcat.threads.max=8 &
wait_healthy identity-service 8081

echo "[core] starting catalog-service"
java $JVM_COMMON -Xmx110m -jar catalog-service.jar --server.port=8082 --server.tomcat.threads.max=8 &
wait_healthy catalog-service 8082

echo "[core] starting booking-service"
java $JVM_COMMON -Xmx90m -jar booking-service.jar --server.port=8083 --server.tomcat.threads.max=8 &
wait_healthy booking-service 8083

echo "[core] starting payment-service"
java $JVM_COMMON -Xmx90m -jar payment-service.jar --server.port=8084 --server.tomcat.threads.max=8 &
wait_healthy payment-service 8084

echo "[core] starting gateway on port ${GATEWAY_PORT} (foreground)"
exec java $JVM_COMMON -Xmx90m -jar gateway.jar --server.port="${GATEWAY_PORT}" --server.tomcat.threads.max=16
