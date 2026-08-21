package com.recruitflow.security.annotation;

import org.springframework.security.access.prepost.PostAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows reading a candidate profile only if it belongs to the caller, or the
 * caller is a RECRUITER. Evaluated after the method returns (uses returnObject).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PostAuthorize("returnObject.userId == authentication.name or hasRole('RECRUITER')")
public @interface CanViewProfile {
}
