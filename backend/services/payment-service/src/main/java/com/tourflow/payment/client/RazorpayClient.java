package com.tourflow.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

// Thin wrapper around Razorpay's Orders REST API. Requests are authenticated
// with HTTP Basic auth using the account's key ID/secret, as Razorpay requires.
@Component
public class RazorpayClient {

    private final RestClient restClient;

    public RazorpayClient(
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .defaultHeaders(headers -> headers.setBasicAuth(keyId, keySecret))
                .build();
    }

    // Create a Razorpay order for the given amount, expressed in the
    // currency's smallest unit (e.g. paise for INR).
    public RazorpayOrder createOrder(long amountInSubunits, String currency, String receipt) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amountInSubunits);
        body.put("currency", currency);
        body.put("receipt", receipt);
        body.put("payment_capture", 1);

        return restClient
                .post()
                .uri("/orders")
                .body(body)
                .retrieve()
                .body(RazorpayOrder.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RazorpayOrder(
            String id,
            long amount,
            String currency,
            String status
    ) {
    }
}
