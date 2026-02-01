package com.meetscribe.app.core.usecase;

import com.meetscribe.app.core.domain.User;
import com.meetscribe.app.core.exception.DomainException;
import com.meetscribe.app.core.port.UserRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class CreateUserUseCase {

    private final UserRepositoryPort repository;

    public CreateUserUseCase(UserRepositoryPort repository) {
        this.repository = repository;
    }

    public User execute(String email, String passwordHash) {

        if (email == null || email.isBlank()) {
            throw new DomainException("Email must not be empty");
        }

        if (!email.contains("@")) {
            throw new DomainException("Invalid email format");
        }

        if (repository.existsByEmail(email)) {
            throw new DomainException("Email already registered");
        }

        return repository.save(email, passwordHash);
    }
}
