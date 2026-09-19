# Standalone deployable: one free Render service (souvik-tourflow-search), which
# sleeps after 15 min idle and cold-starts on its next request. Build context must be the "backend"
# directory; Dockerfile Path on Render is "deploy/search-service/Dockerfile".
#
# Uses Elasticsearch by default (ELASTICSEARCH_URIS + ELASTICSEARCH_API_KEY,
# e.g. Elastic Cloud). Set SEARCH_BACKEND=catalog to skip Elasticsearch and
# query catalog-service via CATALOG_SERVICE_URL instead -- see
# TourSearchService's Javadoc for the tradeoffs.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY deploy/common/Proxy.java /proxy/Proxy.java
RUN javac -d /proxy /proxy/Proxy.java
COPY services/search-service services/search-service
RUN mvn -f services/search-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /proxy/*.class /app/
COPY --from=build /build/services/search-service/target/search-service-0.0.1-SNAPSHOT.jar app.jar

# Proxy binds Render's $PORT immediately and holds requests until the app is up on
# 18080 (see deploy/common/Proxy.java): Render's port-scan window is shorter than
# a Spring Boot cold start on 0.1 vCPU.
EXPOSE 8099
ENTRYPOINT ["sh", "-c", "java -Xmx16m -Xss256k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -cp /app Proxy ${PORT:-8099} 18080 & exec java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx200m -jar app.jar --server.port=18080"]
