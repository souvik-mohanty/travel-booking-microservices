# Bundles the 5 hot-path services (gateway, identity, catalog, booking,
# payment) into ONE deployable so the free Render plan only ever pays one
# cold-start, instead of 5 chained ones -- see docs/decisions/ADR-003-RENDER-DEPLOYMENT.md.
#
# Build context must be the "backend" directory (this file lives at
# backend/deploy/core/Dockerfile so it can COPY sibling service directories);
# on Render, set "Docker Build Context Directory" to "backend" and
# "Dockerfile Path" to "deploy/core/Dockerfile".

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY gateway/api-gateway gateway/api-gateway
COPY services/identity-service services/identity-service
COPY services/catalog-service services/catalog-service
COPY services/booking-service services/booking-service
COPY services/payment-service services/payment-service

RUN mvn -f gateway/api-gateway/pom.xml -q -DskipTests package \
 && mvn -f services/identity-service/pom.xml -q -DskipTests package \
 && mvn -f services/catalog-service/pom.xml -q -DskipTests package \
 && mvn -f services/booking-service/pom.xml -q -DskipTests package \
 && mvn -f services/payment-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app

COPY --from=build /build/services/identity-service/target/identity-service-0.0.1-SNAPSHOT.jar identity-service.jar
COPY --from=build /build/services/catalog-service/target/catalog-service-0.0.1-SNAPSHOT.jar catalog-service.jar
COPY --from=build /build/services/booking-service/target/booking-service-0.0.1-SNAPSHOT.jar booking-service.jar
COPY --from=build /build/services/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar payment-service.jar
COPY --from=build /build/gateway/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar gateway.jar
COPY deploy/core/entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

# Every hot-path service needs DB_*/KAFKA_* env vars pointing at Neon/Upstash --
# set these in Render's dashboard, not here (see the README in this directory).

EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
