package com.recruitflow.security;

import com.recruitflow.repository.JobRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Object-level authorization for jobs: only the recruiter who created a job
 * (or an ADMIN) may edit/delete it. Backs the {@code hasPermission(#id, 'Job', 'edit')}
 * expression used by @CanEditJob.
 */
@Component
public class JobPermissionEvaluator implements PermissionEvaluator {

    private final JobRepository jobRepository;

    public JobPermissionEvaluator(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Domain-object variant not used; we resolve by id below.
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {
        if (authentication == null || targetId == null) {
            return false;
        }
        if ("Job".equals(targetType) && "edit".equals(permission)) {
            return jobRepository.findById(targetId.toString())
                    .map(job -> authentication.getName().equals(job.getCreatedBy()) || isAdmin(authentication))
                    .orElse(false);
        }
        return false;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
