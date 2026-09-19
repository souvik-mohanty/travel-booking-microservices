# Standalone deployable: one free Render service (tourflow-identity), which
# sleeps after 15 min idle and cold-starts on its next request. Build context must be the "backend"
# directory; Dockerfile Path on Render is "deploy/identity-service/Dockerfile".

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/identity-service services/identity-service
RUN mvn -f services/identity-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/services/identity-service/target/identity-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx200m -jar app.jar --server.port=${PORT:-8081}"]
