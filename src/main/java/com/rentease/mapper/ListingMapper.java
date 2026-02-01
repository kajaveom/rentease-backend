package com.rentease.mapper;

import com.rentease.dto.response.ListingResponse;
import com.rentease.dto.response.ListingSummaryResponse;
import com.rentease.entity.Listing;
import com.rentease.entity.ListingImage;
import com.rentease.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListingMapper {

    public ListingResponse toResponse(Listing listing) {
        return ListingResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .category(listing.getCategory())
                .pricePerDay(listing.getPricePerDay())
                .depositAmount(listing.getDepositAmount())
                .condition(listing.getCondition())
                .brand(listing.getBrand())
                .model(listing.getModel())
                .pickupLocation(listing.getPickupLocation())
                .available(listing.getAvailable())
                .images(mapImages(listing.getImages()))
                .owner(mapOwner(listing.getOwner()))
                .createdAt(listing.getCreatedAt())
                .build();
    }

    public ListingSummaryResponse toSummaryResponse(Listing listing) {
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
                .owner(mapOwnerSummary(listing.getOwner()))
                .build();
    }

    private List<ListingResponse.ListingImageResponse> mapImages(List<ListingImage> images) {
        return images.stream()
                .map(img -> ListingResponse.ListingImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    private ListingResponse.OwnerResponse mapOwner(User owner) {
        return ListingResponse.OwnerResponse.builder()
                .id(owner.getId())
                .firstName(owner.getFirstName())
                .lastName(owner.getPublicLastName())
                .avatarUrl(owner.getAvatarUrl())
                .idVerified(owner.isIdVerified())
                // TODO: Add average rating and total reviews when Review entity is implemented
                .averageRating(null)
                .totalReviews(0)
                .build();
    }

    private ListingSummaryResponse.OwnerSummary mapOwnerSummary(User owner) {
        return ListingSummaryResponse.OwnerSummary.builder()
                .id(owner.getId())
                .firstName(owner.getFirstName())
                .idVerified(owner.isIdVerified())
                // TODO: Add average rating when Review entity is implemented
                .averageRating(null)
                .build();
    }
}
