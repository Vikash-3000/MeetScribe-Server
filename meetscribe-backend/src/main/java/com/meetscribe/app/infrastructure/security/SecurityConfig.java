package com.meetscribe.app.infrastructure.security;

import com.meetscribe.app.infrastructure.security.gateway.GatewayOnlyFilter;
import com.meetscribe.app.infrastructure.security.jwt.JwtAuthenticationEntryPoint;
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

    private final CustomOAuth2SuccessHandler oAuth2SuccessHandler;
    private final GatewayOnlyFilter gatewayOnlyFilter;
    private final JwtAuthenticationEntryPoint entryPoint;

    public SecurityConfig(
            CustomOAuth2SuccessHandler oAuth2SuccessHandler,
            GatewayOnlyFilter gatewayOnlyFilter,
            JwtAuthenticationEntryPoint entryPoint
    ) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.gatewayOnlyFilter = gatewayOnlyFilter;
        this.entryPoint = entryPoint;
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

                // ✅ OAuth needs session
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/**",
                                "/actuator/health/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth ->
                        oauth.successHandler(oAuth2SuccessHandler)
                )

                // 🔐 Only gateway can access backend
                .addFilterBefore(
                        gatewayOnlyFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}