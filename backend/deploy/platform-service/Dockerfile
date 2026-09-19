# Standalone deployable: one free Render service (tourflow-platform), which
# sleeps after 15 min idle and cold-starts on its next request. Build context must be the "backend"
# directory; Dockerfile Path on Render is "deploy/platform-service/Dockerfile".
#
# Note: this service writes uploaded files to local disk (file.storage-path)
# -- Render's free tier has no persistent disk, so uploads are lost on every
# restart/redeploy. Fine for a demo; would need real object storage (e.g. S3)
# before this saw real usage.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/platform-service services/platform-service
RUN mvn -f services/platform-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/services/platform-service/target/platform-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8088
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx200m -jar app.jar --server.port=${PORT:-8088}"]
