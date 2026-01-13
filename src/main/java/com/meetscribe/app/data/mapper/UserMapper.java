package com.meetscribe.app.data.mapper;

import com.meetscribe.app.core.domain.User;
import com.meetscribe.app.data.entity.UserEntity;

public class UserMapper {

    private UserMapper() {}

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail()
        );
    }

    public static UserEntity toEntity(
            String email,
            String passwordHash
    ) {
        return new UserEntity(email, passwordHash);
    }
}
