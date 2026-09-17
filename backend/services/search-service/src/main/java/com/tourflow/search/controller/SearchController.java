package com.tourflow.search.controller;

import com.tourflow.search.dto.TourSearchResultResponse;
import com.tourflow.search.service.TourSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final TourSearchService tourSearchService;

    public SearchController(TourSearchService tourSearchService) {
        this.tourSearchService = tourSearchService;
    }

    @GetMapping("/tours")
    public ResponseEntity<TourSearchResultResponse> searchTours(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                tourSearchService.search(q, destination, minPrice, maxPrice, page, size)
        );
    }
}
