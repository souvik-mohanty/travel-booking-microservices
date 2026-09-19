# Standalone deployable -- this service is NOT in the always-warm "core"
# bundle (see deploy/core/Dockerfile), so it free-tier-sleeps independently
# and cold-starts on its own when hit. Build context must be the "backend"
# directory; Dockerfile Path on Render is "deploy/mobility-service/Dockerfile".

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/mobility-service services/mobility-service
RUN mvn -f services/mobility-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/services/mobility-service/target/mobility-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8086
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx200m -jar app.jar --server.port=${PORT:-8086}"]
