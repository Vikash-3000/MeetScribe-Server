package com.meetscribe.app.data.mapper;

import com.meetscribe.app.core.domain.User;
import com.meetscribe.app.data.entity.UserEntity;

public class UserMapper {

    private UserMapper() {}

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getDeviceId()
        );
    }

    public static UserEntity toEntity(
            String email,
            String passwordHash,
            String deviceId
    ) {
        return new UserEntity(email, passwordHash, deviceId);
    }
}
