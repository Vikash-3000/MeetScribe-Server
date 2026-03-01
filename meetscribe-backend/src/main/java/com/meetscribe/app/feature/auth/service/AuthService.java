package com.meetscribe.app.feature.auth.service;

import com.meetscribe.app.core.exception.DomainException;
import com.meetscribe.app.data.entity.RefreshTokenEntity;
import com.meetscribe.app.data.entity.UserEntity;
import com.meetscribe.app.data.repository.RefreshTokenRepository;
import com.meetscribe.app.data.repository.UserJpaRepository;
import com.meetscribe.app.feature.auth.dto.LoginResponse;
import com.meetscribe.app.infrastructure.security.HashUtils;
import com.meetscribe.app.infrastructure.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserJpaRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    @Value("${security.refresh.secret}")
    private String refreshSecret;

    public AuthService(
            UserJpaRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public LoginResponse login(String email,
                               String rawPassword,
                               String deviceId) {

        // 1️⃣ Validate user
        UserEntity user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new DomainException("Email not registered")
                );

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new DomainException("Incorrect password");
        }

        // 🔐 One-device enforcement
        if (user.getDeviceId() != null &&
                !user.getDeviceId().equals(deviceId)) {

            // Delete all previous refresh tokens
            refreshTokenRepository.deleteByUserId(user.getId());
        }

        // Update deviceId
        user.setDeviceId(deviceId);
        user.setTokenVersion(user.getTokenVersion()+1);
        userRepository.save(user);

        // 2️⃣ Generate tokens
        String accessToken =
                jwtProvider.generateToken(
                        user.getId(),
                        user.getEmail(),
                        user.getDeviceId(),
                        user.getTokenVersion()
                );

        String refreshToken = UUID.randomUUID().toString();
        String refreshTokenHash =
                HashUtils.hmacSha256(refreshToken, refreshSecret);

        // 3️⃣ Store refresh token (hash recommended later)
        RefreshTokenEntity refreshEntity =
                new RefreshTokenEntity(
                        user.getId(),
                        refreshTokenHash,
                        deviceId,
                        Instant.now().plusSeconds(7 * 24 * 60 * 60)
                );

        refreshTokenRepository.save(refreshEntity);

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail()
        );
    }

    public LoginResponse refresh(String rawRefreshToken) {

        String hashed =
                HashUtils.hmacSha256(rawRefreshToken, refreshSecret);

        RefreshTokenEntity matchedToken =
                refreshTokenRepository
                        .findByTokenHash(hashed)
                        .orElseThrow(() ->
                                new DomainException("Invalid refresh token")
                        );

        if (matchedToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(matchedToken);
            throw new DomainException("Refresh token expired");
        }

        UserEntity user = userRepository
                .findById(matchedToken.getUserId())
                .orElseThrow(() ->
                        new DomainException("User not found")
                );

        // 🔁 ROTATE
        refreshTokenRepository.delete(matchedToken);

        String newAccessToken =
                jwtProvider.generateToken(
                        user.getId(),
                        user.getEmail(),
                        user.getDeviceId(),
                        user.getTokenVersion()
                );

        String newRefreshToken = UUID.randomUUID().toString();
        String newRefreshHash =
                HashUtils.hmacSha256(newRefreshToken, refreshSecret);

        refreshTokenRepository.save(
                new RefreshTokenEntity(
                        user.getId(),
                        newRefreshHash,
                        user.getDeviceId(),
                        Instant.now().plusSeconds(7 * 24 * 60 * 60)
                )
        );

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                user.getId(),
                user.getEmail()
        );
    }
}