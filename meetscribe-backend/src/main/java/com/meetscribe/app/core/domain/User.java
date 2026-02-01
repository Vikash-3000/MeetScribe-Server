package com.meetscribe.app.core.domain;

import lombok.Getter;

@Getter
public class User {

    private final Long id;
    private final String email;

    public User(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}
