package com.meetscribe.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                // Gateways are stateless
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Gateway does NOT authenticate users
                .authorizeExchange(ex -> ex
                        // Infra
                        .pathMatchers("/actuator/**").permitAll()

                        // Public entry APIs
                        .pathMatchers(
                                "/api/auth/**",   // login
                                "/api/users"      // signup (POST)
                        ).permitAll()

                        // Everything else passes through gateway
                        .anyExchange().permitAll()
                )

                // ❌ NO oauth2ResourceServer() here
                .build();
    }
}
