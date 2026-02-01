package com.meetscribe.app.infrastructure.persistence;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.meetscribe.app.data.repository"
)
@EntityScan(
        basePackages = "com.meetscribe.app.data.entity"
)
public class JpaConfig {
}