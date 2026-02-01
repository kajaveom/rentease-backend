package com.rentease.dto.response;

import com.rentease.entity.Listing;
import com.rentease.entity.enums.Category;
import com.rentease.entity.enums.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingSummaryResponse {

    private UUID id;
    private String title;
    private Category category;
    private Integer pricePerDay;
    private Integer depositAmount;
    private Condition condition;
    private String pickupLocation;
    private Boolean available;
    private String primaryImage;
    private OwnerSummary owner;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerSummary {
        private UUID id;
        private String firstName;
        private Boolean idVerified;
        private Double averageRating;
    }

    public static ListingSummaryResponse fromEntity(Listing listing) {
        return ListingSummaryResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .category(listing.getCategory())
                .pricePerDay(listing.getPricePerDay())
                .depositAmount(listing.getDepositAmount())
                .condition(listing.getCondition())
                .pickupLocation(listing.getPickupLocation())
                .available(listing.getAvailable())
                .primaryImage(listing.getPrimaryImageUrl())
                .owner(OwnerSummary.builder()
                        .id(listing.getOwner().getId())
                        .firstName(listing.getOwner().getFirstName())
                        .idVerified(listing.getOwner().isIdVerified())
                        .averageRating(listing.getOwner().getAverageRatingAsDouble())
                        .build())
                .build();
    }
}
