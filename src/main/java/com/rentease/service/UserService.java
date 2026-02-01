package com.rentease.service;

import com.rentease.dto.request.ChangePasswordRequest;
import com.rentease.dto.request.IdVerificationRequest;
import com.rentease.dto.request.UpdateProfileRequest;
import com.rentease.dto.response.IdVerificationResponse;
import com.rentease.dto.response.PublicProfileResponse;
import com.rentease.dto.response.UserResponse;
import com.rentease.entity.User;
import com.rentease.entity.enums.IdVerificationStatus;
import com.rentease.exception.BadRequestException;
import com.rentease.exception.ResourceNotFoundException;
import com.rentease.mapper.UserMapper;
import com.rentease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return PublicProfileResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio().trim().isEmpty() ? null : request.getBio().trim());
        }

        if (request.getLocation() != null) {
            user.setLocation(request.getLocation().trim().isEmpty() ? null : request.getLocation().trim());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim().isEmpty() ? null : request.getAvatarUrl().trim());
        }

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Check if new password is different
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // For now, we'll do a soft delete by anonymizing the user data
        // In a real app, you'd want to handle cascading deletes of listings, bookings, etc.
        user.setEmail("deleted_" + userId + "@deleted.com");
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setAvatarUrl(null);
        user.setBio(null);
        user.setLocation(null);
        user.setPasswordHash("DELETED");

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public IdVerificationResponse getVerificationStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return IdVerificationResponse.fromEntity(user);
    }

    @Transactional
    public IdVerificationResponse submitVerification(UUID userId, IdVerificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if already verified
        if (user.isIdVerified()) {
            throw new BadRequestException("Your ID is already verified");
        }

        // Check if there's already a pending verification
        if (user.getIdVerificationStatus() == IdVerificationStatus.PENDING) {
            throw new BadRequestException("You already have a pending verification request");
        }

        // Submit for verification
        user.setIdDocumentUrl(request.getDocumentUrl());
        user.setIdVerificationStatus(IdVerificationStatus.PENDING);
        user.setIdVerificationSubmittedAt(LocalDateTime.now());
        user.setIdVerificationRejectionReason(null);

        User saved = userRepository.save(user);
        return IdVerificationResponse.fromEntity(saved);
    }

    @Transactional
    public IdVerificationResponse cancelVerification(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getIdVerificationStatus() != IdVerificationStatus.PENDING) {
            throw new BadRequestException("No pending verification to cancel");
        }

        user.setIdVerificationStatus(IdVerificationStatus.NONE);
        user.setIdDocumentUrl(null);
        user.setIdVerificationSubmittedAt(null);

        User saved = userRepository.save(user);
        return IdVerificationResponse.fromEntity(saved);
    }
}
