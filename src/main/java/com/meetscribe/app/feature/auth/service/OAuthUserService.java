package com.meetscribe.app.feature.auth.service;

import com.meetscribe.app.data.entity.UserEntity;
import com.meetscribe.app.data.repository.UserJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuthUserService {

    private final UserJpaRepository userRepository;

    public OAuthUserService(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity findOrCreate(String email) {

        return userRepository
                .findByEmail(email)
                .orElseGet(() ->
                        userRepository.save(
                                UserEntity.oauthUser(email)
                        )
                );
    }
}
