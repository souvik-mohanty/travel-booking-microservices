package com.tourflow.review.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// A shared RestClient that forwards the current request's Authorization
// header to every outbound call -- so BookingClient/BusinessClient don't
// each need to thread a token parameter through their methods.
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {

                    ServletRequestAttributes attributes =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                    if (attributes != null) {
                        HttpServletRequest httpRequest = attributes.getRequest();

                        String authorization =
                                httpRequest.getHeader("Authorization");

                        if (authorization != null && !authorization.isBlank()) {
                            request.getHeaders().set(
                                    "Authorization",
                                    authorization
                            );
                        }
                    }

                    return execution.execute(request, body);
                })
                .build();
    }
}
