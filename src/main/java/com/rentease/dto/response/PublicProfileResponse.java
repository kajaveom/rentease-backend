package com.rentease.dto.response;

import com.rentease.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PublicProfileResponse {
    private UUID id;
    private String firstName;
    private String lastInitial; // Only show first letter of last name
    private String avatarUrl;
    private String bio;
    private String location;
    private Boolean idVerified;
    private Double averageRating;
    private Integer totalReviews;
    private LocalDateTime memberSince;

    public static PublicProfileResponse fromEntity(User user) {
        String lastInitial = user.getLastName() != null && !user.getLastName().isEmpty()
                ? user.getLastName().charAt(0) + "."
                : null;

        return PublicProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastInitial(lastInitial)
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .location(user.getLocation())
                .idVerified(user.isIdVerified())
                .averageRating(user.getAverageRating())
                .totalReviews(user.getTotalReviews())
                .memberSince(user.getCreatedAt())
                .build();
    }
}
