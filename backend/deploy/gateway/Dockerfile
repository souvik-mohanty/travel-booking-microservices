# Standalone deployable: the API gateway, a separate free Render service
# (tourflow-gateway). Every route target is an environment variable
# (IDENTITY_SERVICE_URL, CATALOG_SERVICE_URL, ... -- see render.yaml), pointing
# at the other services' public Render URLs. Build context must be the
# "backend" directory; Dockerfile Path on Render is "deploy/gateway/Dockerfile".

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY gateway/api-gateway gateway/api-gateway
RUN mvn -f gateway/api-gateway/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/gateway/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx120m -jar app.jar --server.port=${PORT:-8080}"]
