package com.recruitflow.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows the action only when the caller owns the profile being modified.
 * The annotated method MUST have a parameter named {@code userId}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("#userId == authentication.name")
public @interface IsProfileOwner {
}
