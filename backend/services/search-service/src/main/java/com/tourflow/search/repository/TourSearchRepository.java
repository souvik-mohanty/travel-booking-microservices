package com.tourflow.search.repository;

import com.tourflow.search.document.TourDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

// Used only by TourEventListener to index/remove documents. Actual search
// queries go through ElasticsearchOperations in TourSearchService instead,
// since a derived query method can't express "match across three fields
// plus an optional price range" cleanly.
public interface TourSearchRepository extends ElasticsearchRepository<TourDocument, String> {
}
