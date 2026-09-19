# Standalone deployable: one free Render service (souvik-tourflow-identity), which
# sleeps after 15 min idle and cold-starts on its next request. Build context must be the "backend"
# directory; Dockerfile Path on Render is "deploy/identity-service/Dockerfile".

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY deploy/common/Proxy.java /proxy/Proxy.java
RUN javac -d /proxy /proxy/Proxy.java
COPY services/identity-service services/identity-service
RUN mvn -f services/identity-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /proxy/*.class /app/
COPY --from=build /build/services/identity-service/target/identity-service-0.0.1-SNAPSHOT.jar app.jar

# Proxy binds Render's $PORT immediately and holds requests until the app is up on
# 18080 (see deploy/common/Proxy.java): Render's port-scan window is shorter than
# a Spring Boot cold start on 0.1 vCPU.
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java -Xmx16m -Xss256k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -cp /app Proxy ${PORT:-8081} 18080 & exec java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx200m -jar app.jar --server.port=18080"]
