package com.recruitflow.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows only a HIRING_MANAGER who owns the application's department.
 * The annotated method MUST have a parameter named {@code appId}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('HIRING_MANAGER') and @deptSecurity.isManagerForApplication(#appId, authentication.name)")
public @interface IsHiringManagerForApplication {
}
