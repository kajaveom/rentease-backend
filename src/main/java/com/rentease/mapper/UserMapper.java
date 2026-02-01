package com.rentease.mapper;

import com.rentease.dto.response.UserResponse;
import com.rentease.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .location(user.getLocation())
                .idVerified(user.isIdVerified())
                .emailVerified(user.isEmailVerified())
                .averageRating(user.getAverageRatingAsDouble())
                .totalReviews(user.getTotalReviews())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserResponse toPublicResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getPublicLastName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .location(user.getLocation())
                .idVerified(user.isIdVerified())
                .averageRating(user.getAverageRatingAsDouble())
                .totalReviews(user.getTotalReviews())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
