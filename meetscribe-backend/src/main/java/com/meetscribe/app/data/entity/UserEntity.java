package com.meetscribe.app.data.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
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

    @Column(nullable = false)
    private String provider; // LOCAL | GOOGLE

    protected UserEntity() {}

    // For normal signup
    public UserEntity(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.provider = "LOCAL";
    }

    // For OAuth signup
    public static UserEntity oauthUser(String email) {
        UserEntity user = new UserEntity();
        user.email = email;
        user.provider = "GOOGLE";
        return user;
    }
}
