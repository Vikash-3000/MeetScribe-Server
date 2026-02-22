package com.meetscribe.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalGatewayHeaderConfig {

    public static final String INTERNAL_HEADER = "X-Internal-Gateway";
    public static final String INTERNAL_VALUE = "meetscribe-gateway";

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
                    .header(INTERNAL_HEADER, INTERNAL_VALUE)
                    .build();

            return chain.filter(
                    exchange.mutate().request(mutatedRequest).build()
            );
        };
    }
}