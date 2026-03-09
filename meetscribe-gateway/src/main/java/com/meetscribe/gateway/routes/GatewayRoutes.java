package com.meetscribe.gateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutes {

    @Value("${services.backend.uri}")
    private String backendUri;

    // ===============================
    // ROUTES
    // ===============================
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // OAuth
                .route("oauth", r -> r
                        .path("/oauth2/**", "/login/oauth2/**")
                        .uri(backendUri)
                )

                // ===============================
                // BACKEND SERVICE ROUTE
                // ===============================
                // LOGIN – strict
                .route("auth-login", r -> r
                        .path("/api/auth/login")
                        .filters(f -> f
                                .stripPrefix(1)
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(loginRateLimiter());
                                    c.setKeyResolver(keyResolver());
                                })
                        )
                        .uri(backendUri)
                )

                // SIGNUP – very strict
                .route("user-signup", r -> r
                        .path("/api/users")
                        .and()
                        .method("POST")
                        .filters(f -> f
                                .stripPrefix(1)
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(signupRateLimiter());
                                    c.setKeyResolver(keyResolver());
                                })
                        )
                        .uri(backendUri)
                )

                // ALL OTHER APIs
                .route("backend-default", r -> r
                        .path("/api/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(defaultRateLimiter());
                                    c.setKeyResolver(keyResolver());
                                })
                        )
                        .uri(backendUri)
                )

                .build();
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
    public RedisRateLimiter loginRateLimiter() {
        return new RedisRateLimiter(2, 5);
    }

    @Bean
    public RedisRateLimiter signupRateLimiter() {
        return new RedisRateLimiter(1, 3);
    }

    @Bean
    @Primary
    public RedisRateLimiter defaultRateLimiter() {
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
                return Mono.just(authHeader.substring(7));
            }

            if (exchange.getRequest().getRemoteAddress() != null) {
                return Mono.just(
                        exchange.getRequest()
                                .getRemoteAddress()
                                .getAddress()
                                .getHostAddress()
                );
            }

            return Mono.just("anonymous");
        };
    }
}