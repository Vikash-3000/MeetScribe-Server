package com.meetscribe.app.feature.user.dto;

import com.meetscribe.app.core.domain.User;

public record UserResponse(Long id, String email) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail()
        );
    }
}
