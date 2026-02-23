package com.meetscribe.gateway.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter {

    private final JwtValidator jwtValidator;

    // ✅ Constructor injection (CORRECT)
    public JwtAuthFilter(
            @Value("${security.jwt.secret}") String secret
    ) {
        this.jwtValidator = new JwtValidator(secret);
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        String path = exchange.getRequest().getURI().getPath();

        // 🔓 Public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst("ACCESS_TOKEN");

        if (cookie == null) {
            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = cookie.getValue();

        if (jwtValidator.validate(token) == null) {
            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/oauth2")
                || path.startsWith("/login")
                || path.startsWith("/oauth-success")
                || path.startsWith("/actuator");
    }
}