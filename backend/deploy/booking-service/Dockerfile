# Standalone deployable: one free Render service (tourflow-booking), which
# sleeps after 15 min idle and cold-starts on its next request. Build context must be the "backend"
# directory; Dockerfile Path on Render is "deploy/booking-service/Dockerfile".

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/booking-service services/booking-service
RUN mvn -f services/booking-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/services/booking-service/target/booking-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx200m -jar app.jar --server.port=${PORT:-8083}"]
