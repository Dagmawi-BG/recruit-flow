package com.recruitflow.exception;

/** Thrown for invalid/expired/revoked credentials such as a bad refresh token. Maps to 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
