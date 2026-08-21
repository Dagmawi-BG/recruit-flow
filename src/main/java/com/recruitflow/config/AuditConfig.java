package com.recruitflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Enables MongoDB auditing so @CreatedBy/@LastModifiedBy fields are populated with
 * the current username from the security context (or "system" for unauthenticated
 * work such as the DataSeeder).
 */
@Configuration
@EnableMongoAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
                // No authenticated user (e.g. startup seeding, or anonymous "anonymousUser").
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
}
