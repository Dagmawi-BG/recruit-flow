package com.recruitflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Server-side refresh token so sessions can be revoked (logout) — unlike the
 * stateless access token. The opaque token value is the document id.
 */
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    private String username;
    private Instant expiresAt;
    private boolean revoked;

    public RefreshToken() {
    }

    public RefreshToken(String id, String username, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.username = username;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
