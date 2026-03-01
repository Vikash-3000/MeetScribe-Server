package com.meetscribe.app.infrastructure.security;

import com.meetscribe.app.data.repository.UserJpaRepository;
import com.meetscribe.app.infrastructure.security.jwt.JwtAuthenticationEntryPoint;
import com.meetscribe.app.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.meetscribe.app.infrastructure.security.jwt.JwtProvider;
import com.meetscribe.app.infrastructure.security.oauth.CustomOAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private final JwtAuthenticationEntryPoint entryPoint;
    private final JwtProvider jwtProvider;
    private final UserJpaRepository userRepository;

    public SecurityConfig(
            CustomOAuth2SuccessHandler oAuth2SuccessHandler,
            JwtAuthenticationEntryPoint entryPoint,
            JwtProvider jwtProvider,
            UserJpaRepository userRepository
    ) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.entryPoint = entryPoint;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
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
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/actuator/**",
                                "/auth/login",
                                "/auth/refresh",
                                "/users"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth ->
                        oauth.successHandler(oAuth2SuccessHandler)
                )

                // 1️⃣ Validate request came from gateway
                .addFilterBefore(
                        new InternalGatewayValidationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 2️⃣ Then validate JWT
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider, userRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}