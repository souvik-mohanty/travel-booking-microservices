package com.tourflow.search.config;

import com.tourflow.search.document.TourDocument;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.Map;

// Creates the "tours" index (with its field mapping) on first startup, with
// no index settings -- see the note on TourDocument for why Spring Data's own
// automatic creation can't be used. Skipped when SEARCH_BACKEND=catalog, where
// no Elasticsearch instance is expected to exist at all.
@Component
@ConditionalOnProperty(name = "search.backend", havingValue = "elasticsearch", matchIfMissing = true)
public class ToursIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;

    public ToursIndexInitializer(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(TourDocument.class);
        if (!indexOperations.exists()) {
            indexOperations.create(Map.of());
            indexOperations.putMapping();
        }
    }
}
