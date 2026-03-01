package com.meetscribe.app.core.domain;

import lombok.Getter;

@Getter
public class User {

    private final Long id;
    private final String email;
    private final String deviceId;

    public User(Long id, String email, String deviceId) {
        this.id = id;
        this.email = email;
        this.deviceId = deviceId;
    }
}
