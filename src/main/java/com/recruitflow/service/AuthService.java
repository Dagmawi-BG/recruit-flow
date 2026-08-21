package com.recruitflow.service;

import com.recruitflow.dto.request.LoginRequest;
import com.recruitflow.dto.request.RegisterRequest;
import com.recruitflow.dto.response.AuthResponse;
import com.recruitflow.exception.ConflictException;
import com.recruitflow.model.RefreshToken;
import com.recruitflow.model.Role;
import com.recruitflow.model.User;
import com.recruitflow.repository.UserRepository;
import com.recruitflow.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userRepository.findByUsername(request.username()).orElseThrow();
        return buildResponse(user);
    }

    /**
     * Public self-registration. Always creates a CANDIDATE — privileged roles
     * (RECRUITER, HIRING_MANAGER, ADMIN) are provisioned internally, never via signup.
     * The candidate's profile is created lazily on their first profile update.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                Role.CANDIDATE,
                null);
        userRepository.save(user);
        return buildResponse(user);
    }

    /** Exchanges a valid refresh token for a new access token (and rotates the refresh token). */
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.verify(refreshTokenValue);
        User user = userRepository.findByUsername(refreshToken.getUsername()).orElseThrow();
        refreshTokenService.revoke(refreshTokenValue); // rotation: old token can't be reused
        return buildResponse(user);
    }

    /** Revokes the refresh token so it can no longer be used (logout). */
    public void logout(String refreshTokenValue) {
        refreshTokenService.revoke(refreshTokenValue);
    }

    private AuthResponse buildResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user.getUsername());
        return new AuthResponse(accessToken, refreshToken.getId(), user.getUsername(), user.getRole().name());
    }
}
