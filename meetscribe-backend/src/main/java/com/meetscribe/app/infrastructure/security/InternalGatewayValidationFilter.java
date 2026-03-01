package com.meetscribe.app.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class InternalGatewayValidationFilter
        extends OncePerRequestFilter {

    private static final String INTERNAL_HEADER = "X-Internal-Gateway";
    private static final String INTERNAL_VALUE = "meetscribe-gateway";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Allow OAuth callback & health checks
        if (uri.startsWith("/oauth2")
                || uri.startsWith("/login/oauth2")
                || uri.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(INTERNAL_HEADER);

        if (!INTERNAL_VALUE.equals(header)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }
}