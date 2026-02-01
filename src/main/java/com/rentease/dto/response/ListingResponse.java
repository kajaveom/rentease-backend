package com.rentease.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rentease.entity.enums.Category;
import com.rentease.entity.enums.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListingResponse {

    private UUID id;
    private String title;
    private String description;
    private Category category;
    private Integer pricePerDay;
    private Integer depositAmount;
    private Condition condition;
    private String brand;
    private String model;
    private String pickupLocation;
    private Boolean available;
    private List<ListingImageResponse> images;
    private OwnerResponse owner;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListingImageResponse {
        private UUID id;
        private String imageUrl;
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private Boolean idVerified;
        private Double averageRating;
        private Integer totalReviews;
    }
}
