package com.meetscribe.app.infrastructure.security.jwt;

import com.meetscribe.app.data.entity.UserEntity;
import com.meetscribe.app.data.repository.UserJpaRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserJpaRepository userRepository;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            UserJpaRepository userRepository
    ) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            try {
                Claims claims = jwtProvider.validate(token);

                Long userId = Long.valueOf(claims.getSubject());
                String deviceId = claims.get("deviceId", String.class);
                Integer tokenVersion =
                        claims.get("tokenVersion", Integer.class);

                UserEntity user = userRepository
                        .findById(userId)
                        .orElse(null);

                if (user == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 🔐 Token version validation
                if (!user.getTokenVersion().equals(tokenVersion)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 🔐 Device validation
                if (user.getDeviceId() != null &&
                        !user.getDeviceId().equals(deviceId)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

            } catch (Exception ignored) {
                // handled by entry point
            }
        }

        filterChain.doFilter(request, response);
    }
}