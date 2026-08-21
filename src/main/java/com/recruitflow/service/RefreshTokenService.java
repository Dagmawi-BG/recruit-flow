package com.recruitflow.service;

import com.recruitflow.exception.UnauthorizedException;
import com.recruitflow.model.RefreshToken;
import com.recruitflow.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.repository = repository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public RefreshToken create(String username) {
        RefreshToken token = new RefreshToken(
                UUID.randomUUID().toString(),
                username,
                Instant.now().plusMillis(refreshExpirationMs),
                false);
        return repository.save(token);
    }

    public RefreshToken verify(String tokenValue) {
        RefreshToken token = repository.findById(tokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (token.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }
        return token;
    }

    public void revoke(String tokenValue) {
        repository.findById(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            repository.save(token);
        });
    }
}
