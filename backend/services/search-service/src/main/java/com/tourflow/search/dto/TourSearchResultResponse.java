package com.tourflow.search.dto;

import java.util.List;

public record TourSearchResultResponse(
        List<TourSearchResponse> results,
        long totalResults,
        int page,
        int size
) {
}
