package com.meetscribe.app.feature.auth.dto;

public record LoginResponse(
        String token,
        Long userId,
        String email
) {}

