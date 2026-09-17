package com.tourflow.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

// Every backend call from the browser now goes through this gateway (see
// application.yml's routes) instead of hitting each service directly --
// each downstream service's own CorsConfigurationSource bean is irrelevant
// to the browser's CORS check once a request is proxied through here,
// since the gateway's own response headers are what the browser actually
// evaluates. A plain CorsFilter (registered ahead of the gateway's own
// routing) is used rather than relying on a per-route/per-controller CORS
// annotation, since routed requests aren't dispatched to an
// @RequestMapping controller the usual way.
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter(
            @Value("${app.cors.allowed-origins}") String[] allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        // Run before Spring Cloud Gateway's own routing filters so a
        // preflight OPTIONS request is answered here, not forwarded
        // downstream as if it were a real proxied request.
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    // CorsFilter above already decided the CORS response headers for the
    // browser based on this request's real Origin. But several downstream
    // services (identity-service, catalog-service, ...) have their own
    // CorsConfigurationSource bean too, left over from when the frontend
    // called each service directly -- if the proxied request still carries
    // the Origin header, that downstream bean adds its OWN
    // Access-Control-Allow-Origin header, and the gateway's proxy forwards
    // both back to the browser as a duplicate value, which browsers reject
    // outright. Stripping Origin here (after CorsFilter has already run)
    // stops any downstream service from ever seeing a "cross-origin" look
    // to a proxied request.
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> stripOriginForProxying() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {
                filterChain.doFilter(new OriginStrippingRequest(request), response);
            }
        };

        FilterRegistrationBean<OncePerRequestFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return bean;
    }

    private static final class OriginStrippingRequest extends HttpServletRequestWrapper {

        OriginStrippingRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            return "Origin".equalsIgnoreCase(name) ? null : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return "Origin".equalsIgnoreCase(name) ? Collections.emptyEnumeration() : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(
                    Collections.list(super.getHeaderNames()).stream()
                            .filter(name -> !"Origin".equalsIgnoreCase(name))
                            .toList()
            );
        }
    }
}
