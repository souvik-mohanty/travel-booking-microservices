package com.tourflow.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.tourflow.search.document.TourDocument;
import com.tourflow.search.dto.TourSearchResponse;
import com.tourflow.search.dto.TourSearchResultResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Default search backend: queries the Elasticsearch index TourEventListener
// keeps in sync from tour.events. Replaced by CatalogFallbackTourSearchService
// only when SEARCH_BACKEND=catalog.
@Service
@ConditionalOnProperty(name = "search.backend", havingValue = "elasticsearch", matchIfMissing = true)
public class ElasticsearchTourSearchService implements TourSearchService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticsearchTourSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // Built directly against the co.elastic.clients Query DSL rather than
    // Spring Data Elasticsearch's Criteria fluent API: a chained
    // field().contains(q).or(field2).contains(q) Criteria silently returned
    // zero hits for indexed documents in manual testing (destination-only
    // filtering worked fine, so the bug was specific to that OR chain) --
    // multi_match is also just the standard, well-documented tool for
    // "search across N fields" and removes the ambiguity entirely.
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

        List<Query> must = new ArrayList<>();

        if (StringUtils.hasText(q)) {
            String searchText = q;
            must.add(Query.of(query -> query.multiMatch(m -> m
                    .query(searchText)
                    .fields("title", "description", "destination")
            )));
        }
        if (StringUtils.hasText(destination)) {
            String destinationText = destination;
            must.add(Query.of(query -> query.match(m -> m
                    .field("destination")
                    .query(destinationText)
            )));
        }
        if (minPrice != null || maxPrice != null) {
            must.add(Query.of(query -> query.range(r -> r.number(n -> {
                n.field("price");
                if (minPrice != null) n.gte(minPrice.doubleValue());
                if (maxPrice != null) n.lte(maxPrice.doubleValue());
                return n;
            }))));
        }

        Query rootQuery = must.isEmpty()
                ? Query.of(query -> query.matchAll(ma -> ma))
                : Query.of(query -> query.bool(b -> b.must(must)));

        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size));
        NativeQuery nativeQuery = NativeQuery.builder().withQuery(rootQuery).withPageable(pageable).build();

        SearchHits<TourDocument> hits = elasticsearchOperations.search(nativeQuery, TourDocument.class);

        return new TourSearchResultResponse(
                hits.stream().map(hit -> TourSearchResponse.from(hit.getContent())).toList(),
                hits.getTotalHits(),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }

    private int clampSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
