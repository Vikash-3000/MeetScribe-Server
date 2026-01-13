package com.meetscribe.app.core.port;

import com.meetscribe.app.core.domain.User;

public interface UserRepositoryPort {

    boolean existsByEmail(String email);

    User save(String email, String passwordHash);
}
