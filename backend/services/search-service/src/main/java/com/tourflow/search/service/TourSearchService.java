package com.tourflow.search.service;

import com.tourflow.search.dto.TourSearchResultResponse;

import java.math.BigDecimal;

// Two implementations, chosen by the search.backend property (SEARCH_BACKEND
// env var): ElasticsearchTourSearchService (default) queries the "tours" index
// kept in sync by TourEventListener; CatalogFallbackTourSearchService
// ("catalog") is for deployments with no Elasticsearch instance -- see that
// class for why.
public interface TourSearchService {

    TourSearchResultResponse search(
            String authorizationHeader,
            String q,
            String destination,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size
    );
}
