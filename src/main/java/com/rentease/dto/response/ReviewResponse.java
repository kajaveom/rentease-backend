package com.rentease.dto.response;

import com.rentease.entity.Review;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID bookingId;
    private ListingInfo listing;
    private ReviewerInfo reviewer;
    private Integer rating;
    private String comment;
    private String ownerResponse;
    private LocalDateTime ownerResponseAt;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class ListingInfo {
        private UUID id;
        private String title;
        private String primaryImage;
    }

    @Data
    @Builder
    public static class ReviewerInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
    }

    public static ReviewResponse fromEntity(Review review) {
        String primaryImage = null;
        if (review.getListing().getImages() != null && !review.getListing().getImages().isEmpty()) {
            primaryImage = review.getListing().getImages().get(0).getImageUrl();
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .listing(ListingInfo.builder()
                        .id(review.getListing().getId())
                        .title(review.getListing().getTitle())
                        .primaryImage(primaryImage)
                        .build())
                .reviewer(ReviewerInfo.builder()
                        .id(review.getReviewer().getId())
                        .firstName(review.getReviewer().getFirstName())
                        .lastName(review.getReviewer().getLastName())
                        .avatarUrl(review.getReviewer().getAvatarUrl())
                        .build())
                .rating(review.getRating())
                .comment(review.getComment())
                .ownerResponse(review.getOwnerResponse())
                .ownerResponseAt(review.getOwnerResponseAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
