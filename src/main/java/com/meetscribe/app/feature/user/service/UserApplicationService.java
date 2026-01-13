package com.meetscribe.app.feature.user.service;

import com.meetscribe.app.core.domain.User;
import com.meetscribe.app.core.usecase.CreateUserUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationService {

    private final CreateUserUseCase useCase;
    private final PasswordEncoder passwordEncoder;

    public UserApplicationService(
            CreateUserUseCase useCase,
            PasswordEncoder passwordEncoder
    ) {
        this.useCase = useCase;
        this.passwordEncoder = passwordEncoder;
    }

    public User create(String email, String password) {
        String hash = passwordEncoder.encode(password);
        return useCase.execute(email, hash);
    }
}