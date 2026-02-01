package com.rentease.dto.response;

import com.rentease.entity.Booking;
import com.rentease.entity.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID id;
    private ListingSummaryResponse listing;
    private UserSummaryResponse renter;
    private UserSummaryResponse owner;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private Integer dailyRate;
    private Integer totalPrice;
    private Integer depositAmount;
    private Integer serviceFee;
    private BookingStatus status;
    private String renterMessage;
    private String ownerResponse;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    @Data
    @Builder
    public static class UserSummaryResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private Boolean idVerified;
        private Double averageRating;
        private Integer totalReviews;
    }

    public static BookingResponse fromEntity(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .listing(ListingSummaryResponse.fromEntity(booking.getListing()))
                .renter(mapUserSummary(booking.getRenter()))
                .owner(mapUserSummary(booking.getListing().getOwner()))
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .totalDays(booking.getTotalDays())
                .dailyRate(booking.getDailyRate())
                .totalPrice(booking.getTotalPrice())
                .depositAmount(booking.getDepositAmount())
                .serviceFee(booking.getServiceFee())
                .status(booking.getStatus())
                .renterMessage(booking.getRenterMessage())
                .ownerResponse(booking.getOwnerResponse())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .approvedAt(booking.getApprovedAt())
                .paidAt(booking.getPaidAt())
                .completedAt(booking.getCompletedAt())
                .cancelledAt(booking.getCancelledAt())
                .build();
    }

    private static UserSummaryResponse mapUserSummary(com.rentease.entity.User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .idVerified(user.isIdVerified())
                .averageRating(user.getAverageRatingAsDouble())
                .totalReviews(user.getTotalReviews())
                .build();
    }
}
