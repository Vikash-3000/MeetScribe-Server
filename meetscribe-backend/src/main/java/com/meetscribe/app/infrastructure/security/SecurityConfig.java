package com.meetscribe.app.infrastructure.security;

import com.meetscribe.app.infrastructure.security.gateway.GatewayOnlyFilter;
import com.meetscribe.app.infrastructure.security.jwt.JwtAuthenticationEntryPoint;
import com.meetscribe.app.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.meetscribe.app.infrastructure.security.oauth.CustomOAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final CustomOAuth2SuccessHandler oAuth2SuccessHandler;
    private final GatewayOnlyFilter gatewayOnlyFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            JwtAuthenticationEntryPoint entryPoint,
            CustomOAuth2SuccessHandler oAuth2SuccessHandler,
            GatewayOnlyFilter gatewayOnlyFilter
    ) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.gatewayOnlyFilter = gatewayOnlyFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(entryPoint)
                )
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // ✅ PUBLIC: SIGNUP (MUST COME FIRST)
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()

                        // ✅ PUBLIC: LOGIN
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // ✅ PUBLIC: OAuth + Health
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/**",
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        // 🔒 PROTECTED: EVERYTHING ELSE UNDER /api/users
                        .requestMatchers("/users/**").authenticated()

                        // 🔒 FALLBACK
                        .anyRequest().authenticated()
                )

                // ✅ THIS WAS MISSING
                .oauth2Login(oauth ->
                        oauth.successHandler(oAuth2SuccessHandler)
                )

                // 🔐 Gateway-only enforcement
                .addFilterBefore(
                        gatewayOnlyFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                // 🔐 JWT authentication
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}