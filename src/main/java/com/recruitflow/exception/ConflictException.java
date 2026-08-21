package com.recruitflow.exception;

/** Thrown when a request conflicts with existing state (e.g. duplicate username). Maps to 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
