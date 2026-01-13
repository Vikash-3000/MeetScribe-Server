package com.meetscribe.app.feature.auth.service;

import com.meetscribe.app.core.exception.DomainException;
import com.meetscribe.app.data.entity.UserEntity;
import com.meetscribe.app.data.repository.UserJpaRepository;
import com.meetscribe.app.infrastructure.security.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(
            UserJpaRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public String login(String email, String rawPassword) {

        // 1️⃣ Check email existence
        UserEntity user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new DomainException("Email not registered")
                );

        // 2️⃣ Check password
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new DomainException("Incorrect password");
        }

        // 3️⃣ Generate JWT
        return jwtProvider.generateToken(
                user.getId(),
                user.getEmail()
        );
    }
}