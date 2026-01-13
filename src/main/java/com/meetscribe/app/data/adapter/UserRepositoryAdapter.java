package com.meetscribe.app.data.adapter;

import com.meetscribe.app.core.domain.User;
import com.meetscribe.app.core.port.UserRepositoryPort;
import com.meetscribe.app.data.entity.UserEntity;
import com.meetscribe.app.data.mapper.UserMapper;
import com.meetscribe.app.data.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public User save(String email, String passwordHash) {
        UserEntity entity =
                UserMapper.toEntity(email, passwordHash);

        return UserMapper.toDomain(
                jpaRepository.save(entity)
        );
    }
}
