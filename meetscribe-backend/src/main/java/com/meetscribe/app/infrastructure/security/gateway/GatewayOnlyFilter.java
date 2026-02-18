package com.meetscribe.app.infrastructure.security.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GatewayOnlyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Gateway";
    private static final String EXPECTED_VALUE = "meetscribe-gateway";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Allow infra & auth endpoints
        if (path.startsWith("/actuator")
                || path.startsWith("/oauth2")
                || path.startsWith("/login")
                || path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String gatewayHeader = request.getHeader(HEADER);

        if (!EXPECTED_VALUE.equals(gatewayHeader)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            response.getWriter().write("""
                {
                  "success": false,
                  "error": {
                    "code": "FORBIDDEN",
                    "message": "Direct access to backend is forbidden"
                  }
                }
                """);

            response.getWriter().flush();
            return; // 🔴 THIS IS CRITICAL
        }

        filterChain.doFilter(request, response);
    }
}