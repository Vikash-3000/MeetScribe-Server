package com.meetscribe.app.infrastructure.security.oauth;

import com.meetscribe.app.data.entity.UserEntity;
import com.meetscribe.app.feature.auth.service.OAuthUserService;
import com.meetscribe.app.infrastructure.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final OAuthUserService oauthUserService;

    public CustomOAuth2SuccessHandler(
            JwtProvider jwtProvider,
            OAuthUserService oauthUserService
    ) {
        this.jwtProvider = jwtProvider;
        this.oauthUserService = oauthUserService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User =
                (OAuth2User) authentication.getPrincipal();

        assert oAuth2User != null;
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            throw new RuntimeException("Email not found from OAuth provider");
        }

        UserEntity user =
                oauthUserService.findOrCreate(email);

        // create or fetch user (service call later)
        String token =
                jwtProvider.generateToken(
                        user.getId(),
                        user.getEmail()
                );

        response.sendRedirect(
                "http://localhost:8080/oauth-success?token=" + token
        );
    }
}