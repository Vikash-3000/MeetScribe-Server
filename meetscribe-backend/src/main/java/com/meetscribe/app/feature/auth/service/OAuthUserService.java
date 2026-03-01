package com.meetscribe.app.feature.auth.service;

import com.meetscribe.app.data.entity.RefreshTokenEntity;
import com.meetscribe.app.data.entity.UserEntity;
import com.meetscribe.app.data.repository.RefreshTokenRepository;
import com.meetscribe.app.data.repository.UserJpaRepository;
import com.meetscribe.app.feature.auth.dto.LoginResponse;
import com.meetscribe.app.infrastructure.security.HashUtils;
import com.meetscribe.app.infrastructure.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OAuthUserService {

    private final UserJpaRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    @Value("${security.refresh.secret}")
    private String refreshSecret;

    public OAuthUserService(
            UserJpaRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
    }

    public LoginResponse oauthLogin(String email, String deviceId) {

        UserEntity user = userRepository
                .findByEmail(email)
                .orElseGet(() ->
                        userRepository.save(
                                UserEntity.oauthUser(email, deviceId)
                        )
                );

        // 🔐 One-device enforcement
        if (user.getDeviceId() != null &&
                !user.getDeviceId().equals(deviceId)) {

            refreshTokenRepository.deleteByUserId(user.getId());
        }

        user.setDeviceId(deviceId);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        String accessToken =
                jwtProvider.generateToken(
                        user.getId(),
                        user.getEmail(),
                        user.getDeviceId(),
                        user.getTokenVersion()
                );

        String refreshToken = UUID.randomUUID().toString();
        String hashedRFToken =
                HashUtils.hmacSha256(refreshToken, refreshSecret);

        refreshTokenRepository.save(
                new RefreshTokenEntity(
                        user.getId(),
                        hashedRFToken,
                        deviceId,
                        Instant.now().plusSeconds(7 * 24 * 60 * 60)
                )
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail()
        );
    }
}
