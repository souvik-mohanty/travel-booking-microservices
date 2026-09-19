package com.tourflow.search.service;

import com.tourflow.search.dto.TourSearchResponse;
import com.tourflow.search.dto.TourSearchResultResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

// Opt-in alternative to ElasticsearchTourSearchService (SEARCH_BACKEND=catalog),
// for deployments with no Elasticsearch instance -- TourEventListener's
// index-sync is pointless without one, so it's switched off too. Calls
// catalog-service's own (authenticated) GET /api/tours directly --
// forwarding the caller's own bearer token, since /api/search/tours already
// requires authentication -- and does the filtering/paging in memory instead
// of against a search index. Fine at demo scale; would need to move back to a
// real search index before this saw production traffic.
@Service
@ConditionalOnProperty(name = "search.backend", havingValue = "catalog")
public class CatalogFallbackTourSearchService implements TourSearchService {

    private static final int MAX_PAGE_SIZE = 100;

    private final RestClient restClient;

    public CatalogFallbackTourSearchService(
            @Value("${services.catalog.base-url:http://localhost:8082}") String catalogServiceBaseUrl
    ) {
        this.restClient = RestClient.builder().baseUrl(catalogServiceBaseUrl).build();
    }

    @Override
    public TourSearchResultResponse search(
            String authorizationHeader,
            String q,
            String destination,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size
    ) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }

        CatalogTour[] response = restClient.get()
                .uri("/api/tours")
                .headers(headers -> {
                    if (StringUtils.hasText(authorizationHeader)) {
                        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
                    }
                })
                .retrieve()
                .body(CatalogTour[].class);
        List<CatalogTour> tours = response == null ? List.of() : List.of(response);

        String qLower = StringUtils.hasText(q) ? q.toLowerCase(Locale.ROOT) : null;
        String destinationLower = StringUtils.hasText(destination) ? destination.toLowerCase(Locale.ROOT) : null;

        List<TourSearchResponse> filtered = tours.stream()
                .filter(t -> "PUBLISHED".equals(t.status()))
                .filter(t -> qLower == null
                        || containsIgnoreCase(t.title(), qLower)
                        || containsIgnoreCase(t.description(), qLower)
                        || containsIgnoreCase(t.destination(), qLower))
                .filter(t -> destinationLower == null || containsIgnoreCase(t.destination(), destinationLower))
                .filter(t -> minPrice == null || (t.price() != null && t.price().compareTo(minPrice) >= 0))
                .filter(t -> maxPrice == null || (t.price() != null && t.price().compareTo(maxPrice) <= 0))
                .sorted(Comparator.comparing(CatalogTour::startDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(CatalogTour::toSearchResponse)
                .toList();

        int clampedSize = clampSize(size);
        int fromIndex = Math.min(Math.max(page, 0) * clampedSize, filtered.size());
        int toIndex = Math.min(fromIndex + clampedSize, filtered.size());

        return new TourSearchResultResponse(
                filtered.subList(fromIndex, toIndex),
                filtered.size(),
                Math.max(page, 0),
                clampedSize
        );
    }

    private boolean containsIgnoreCase(String value, String needleLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needleLower);
    }

    private int clampSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, MAX_PAGE_SIZE);
    }

    // Mirrors catalog-service's TourResponse -- only the fields this fallback
    // needs. Unknown fields (createdAt, updatedAt, ...) are ignored by Jackson.
    private record CatalogTour(
            UUID id,
            String title,
            String description,
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal price,
            Integer maxParticipants,
            String status,
            UUID createdBy
    ) {
        TourSearchResponse toSearchResponse() {
            return new TourSearchResponse(
                    id, title, description, destination, startDate, endDate,
                    price == null ? null : price.doubleValue(), maxParticipants, createdBy
            );
        }
    }
}
