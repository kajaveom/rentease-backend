package com.rentease.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.rentease.dto.request.GoogleAuthRequest;
import com.rentease.dto.request.LoginRequest;
import com.rentease.dto.request.RefreshTokenRequest;
import com.rentease.dto.request.RegisterRequest;
import com.rentease.dto.response.AuthResponse;
import com.rentease.dto.response.UserResponse;
import com.rentease.entity.User;
import com.rentease.exception.BadRequestException;
import com.rentease.exception.UnauthorizedException;
import com.rentease.mapper.UserMapper;
import com.rentease.repository.UserRepository;
import com.rentease.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Value("${app.google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier googleVerifier;

    @PostConstruct
    public void init() {
        if (googleClientId != null && !googleClientId.isEmpty()) {
            googleVerifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
        }
    }

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        if (googleVerifier == null) {
            throw new BadRequestException("Google authentication is not configured");
        }

        try {
            GoogleIdToken idToken = googleVerifier.verify(request.getCredential());
            if (idToken == null) {
                throw new UnauthorizedException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");
            boolean emailVerified = payload.getEmailVerified();

            // Check if user exists
            User user = userRepository.findByEmail(email.toLowerCase()).orElse(null);

            if (user == null) {
                // Create new user
                user = User.builder()
                        .email(email.toLowerCase())
                        .passwordHash("GOOGLE_SSO") // No password for SSO users
                        .firstName(firstName != null ? firstName : "User")
                        .lastName(lastName != null ? lastName : "")
                        .avatarUrl(pictureUrl)
                        .emailVerified(emailVerified)
                        .build();
                user = userRepository.save(user);
                log.info("New user registered via Google: {}", email);
            } else {
                // Update existing user's avatar if not set
                if (user.getAvatarUrl() == null && pictureUrl != null) {
                    user.setAvatarUrl(pictureUrl);
                    user = userRepository.save(user);
                }
                log.info("User logged in via Google: {}", email);
            }

            return generateAuthResponse(user);

        } catch (Exception e) {
            log.error("Google authentication failed", e);
            throw new UnauthorizedException("Google authentication failed: " + e.getMessage());
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BadRequestException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        log.info("User logged in: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.builder()
                .user(userResponse)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
