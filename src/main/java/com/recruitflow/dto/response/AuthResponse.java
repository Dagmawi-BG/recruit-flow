package com.recruitflow.dto.response;

public record AuthResponse(
        String token,
        String refreshToken,
        String username,
        String role
) {
}
