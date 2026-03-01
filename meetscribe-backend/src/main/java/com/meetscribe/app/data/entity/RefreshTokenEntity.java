package com.meetscribe.app.data.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    private String deviceId;

    private Instant expiresAt;

    protected RefreshTokenEntity() {}

    public RefreshTokenEntity(
            Long userId,
            String tokenHash,
            String deviceId,
            Instant expiresAt
    ) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.deviceId = deviceId;
        this.expiresAt = expiresAt;
    }
}