package com.recruitflow.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows editing/deleting a job only if the caller created it (or is an ADMIN),
 * evaluated by {@link com.recruitflow.security.JobPermissionEvaluator}.
 * The annotated method MUST have a parameter named {@code id}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasPermission(#id, 'Job', 'edit')")
public @interface CanEditJob {
}
