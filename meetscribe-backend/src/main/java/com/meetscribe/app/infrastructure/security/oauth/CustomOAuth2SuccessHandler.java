package com.meetscribe.app.infrastructure.security.oauth;

import com.meetscribe.app.feature.auth.dto.LoginResponse;
import com.meetscribe.app.feature.auth.service.OAuthUserService;
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

    private final OAuthUserService oauthUserService;

    public CustomOAuth2SuccessHandler(
            OAuthUserService oauthUserService
    ) {
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

        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            throw new RuntimeException("Email not found from OAuth provider");
        }

        // 🔥 Get deviceId from query param
        String deviceId = request.getParameter("deviceId");

        if (deviceId == null || deviceId.isBlank()) {
            deviceId = "oauth-" + java.util.UUID.randomUUID();
        }

        // 🔐 Enforce device + refresh logic inside service
        LoginResponse loginResponse =
                oauthUserService.oauthLogin(email, deviceId);

        response.setContentType("application/json");
        response.getWriter().write("""
        {
            "accessToken": "%s",
            "refreshToken": "%s",
            "userId": %d,
            "email": "%s"
        }
        """.formatted(
                loginResponse.accessToken(),
                loginResponse.refreshToken(),
                loginResponse.userId(),
                loginResponse.email()
        ));
    }
}