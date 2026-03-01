package com.meetscribe.app.feature.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String email
) {}

