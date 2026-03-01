package com.meetscribe.app.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String passwordHash;

    @Column
    private String deviceId;

    @Column(nullable = false)
    private Integer tokenVersion = 0;

    @Column(nullable = false)
    private String provider; // LOCAL | GOOGLE

    protected UserEntity() {}

    // For normal signup
    public UserEntity(String email, String passwordHash, String deviceId) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.provider = "LOCAL";
        this.deviceId = deviceId;
    }

    // For OAuth signup
    public static UserEntity oauthUser(String email, String deviceId) {
        UserEntity user = new UserEntity();
        user.email = email;
        user.provider = "GOOGLE";
        user.deviceId = deviceId;

        return user;
    }
}
