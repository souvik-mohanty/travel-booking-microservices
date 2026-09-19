# Standalone deployable -- this service is NOT in the always-warm "core"
# bundle (see deploy/core/Dockerfile), so it free-tier-sleeps independently
# and cold-starts on its own when hit. Build context must be the "backend"
# directory; Dockerfile Path on Render is "deploy/insights-service/Dockerfile".

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/insights-service services/insights-service
RUN mvn -f services/insights-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/services/insights-service/target/insights-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8089
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx200m -jar app.jar --server.port=${PORT:-8089}"]
