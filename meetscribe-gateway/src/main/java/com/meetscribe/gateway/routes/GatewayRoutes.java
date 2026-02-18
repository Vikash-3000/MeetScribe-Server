package com.meetscribe.gateway.routes;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutes {

    // ===============================
    // ROUTES
    // ===============================
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ===============================
                // BACKEND SERVICE ROUTE
                // ===============================
                .route("meetscribe-backend", r -> r
                        .path("/api/**")
                        .filters(f -> f
                                // Remove "/api" before forwarding
                                .stripPrefix(1)

                                // Rate limiting (Redis-backed)
                                .requestRateLimiter(config -> {
                                    config.setRateLimiter(redisRateLimiter());
                                    config.setKeyResolver(keyResolver());
                                })
                        )
                        .uri("http://meetscribe-backend:8081")
                )

                .build();
    }

    // ===============================
    // GLOBAL FILTERS
    // ===============================

    /**
     * Adds an internal trust header so backend can verify
     * that traffic ONLY comes from API Gateway.
     */
    @Bean
    public GlobalFilter internalGatewayHeaderFilter() {
        return (exchange, chain) -> {
            var mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-Internal-Gateway", "meetscribe-gateway")
                    .build();

            return chain.filter(
                    exchange.mutate().request(mutatedRequest).build()
            );
        };
    }

    // ===============================
    // RATE LIMITING
    // ===============================

    /**
     * Redis-based rate limiter.
     *
     * replenishRate = 10 requests / second
     * burstCapacity = 20 requests
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }

    /**
     * Rate-limit strategy:
     * - If JWT exists → rate limit per user/token
     * - Else → rate limit as "anonymous"
     */
    @Bean
    public KeyResolver keyResolver() {
        return exchange -> {
            String authHeader =
                    exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Use token (or userId extracted from token later)
                return Mono.just(authHeader.substring(7));
            }

            return Mono.just("anonymous");
        };
    }
}