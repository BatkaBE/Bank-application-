package com.golomt.gateway.GMTConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfiguration {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",  // Next.js frontend
            "http://localhost:8085",  // Gateway
            "https://your-domain.com" // Production domain
    );

    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS, PATCH";
    private static final String ALLOWED_HEADERS = "Authorization, Content-Type, Accept, X-Requested-With, Cache-Control";
    private static final String EXPOSED_HEADERS = "Authorization, Content-Disposition";
    private static final String MAX_AGE = "3600";

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            if (CorsUtils.isPreFlightRequest(request)) {
                String origin = request.getHeaders().getOrigin();
                if (isOriginAllowed(origin)) {
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSED_HEADERS);
                    response.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return Mono.empty();
            }

            String origin = request.getHeaders().getOrigin();
            if (isOriginAllowed(origin)) {
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSED_HEADERS);
                response.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
            }

            return chain.filter(exchange);
        };
    }

    private static boolean isOriginAllowed(String origin) {
        return origin != null && ALLOWED_ORIGINS.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(origin));
    }
}