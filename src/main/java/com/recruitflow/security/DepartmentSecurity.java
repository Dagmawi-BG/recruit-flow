package com.recruitflow.security;

import com.recruitflow.model.Application;
import com.recruitflow.model.Role;
import com.recruitflow.model.User;
import com.recruitflow.repository.ApplicationRepository;
import com.recruitflow.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Custom SpEL security bean referenced as {@code @deptSecurity} in @PreAuthorize.
 * Ensures a HIRING_MANAGER can only act on applications in their own department.
 */
@Component("deptSecurity")
public class DepartmentSecurity {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public DepartmentSecurity(ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    public boolean isManagerForApplication(String appId, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getRole() != Role.HIRING_MANAGER || user.getDepartment() == null) {
            return false;
        }
        Application application = applicationRepository.findById(appId).orElse(null);
        if (application == null) {
            return false;
        }
        return user.getDepartment().equals(application.getDepartment());
    }
}
