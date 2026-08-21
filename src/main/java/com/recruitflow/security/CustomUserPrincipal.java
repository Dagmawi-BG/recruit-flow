package com.recruitflow.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Authenticated principal carrying app-specific identity (userId, role, department)
 * so controllers can use {@code @AuthenticationPrincipal CustomUserPrincipal} instead
 * of re-querying the database.
 */
public class CustomUserPrincipal implements UserDetails {

    private final String userId;
    private final String username;
    private final String password;
    private final String role;
    private final String department;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(String userId, String username, String password, String role,
                               String department, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
        this.authorities = authorities;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
