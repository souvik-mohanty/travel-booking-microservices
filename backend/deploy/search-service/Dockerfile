# Standalone deployable: one free Render service (tourflow-search), which
# sleeps after 15 min idle and cold-starts on its next request. Build context must be the "backend"
# directory; Dockerfile Path on Render is "deploy/search-service/Dockerfile".
#
# Uses Elasticsearch by default (ELASTICSEARCH_URIS + ELASTICSEARCH_API_KEY,
# e.g. Elastic Cloud). Set SEARCH_BACKEND=catalog to skip Elasticsearch and
# query catalog-service via CATALOG_SERVICE_URL instead -- see
# TourSearchService's Javadoc for the tradeoffs.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY services/search-service services/search-service
RUN mvn -f services/search-service/pom.xml -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/services/search-service/target/search-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8099
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -Xmx200m -jar app.jar --server.port=${PORT:-8099}"]
