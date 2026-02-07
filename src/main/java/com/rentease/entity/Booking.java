package com.rentease.entity;

import com.rentease.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "daily_rate", nullable = false)
    private Integer dailyRate; // Price per day at time of booking (cents)

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice; // Total rental price estimate (cents)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "renter_message", length = 500)
    private String renterMessage; // Initial message from renter

    @Column(name = "owner_response", length = 500)
    private String ownerResponse; // Owner's response to request

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Helper method to calculate total days
    @PrePersist
    @PreUpdate
    public void calculateTotalDays() {
        if (startDate != null && endDate != null) {
            this.totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        }
    }

    // Check if the current user is the owner
    public boolean isOwner(UUID userId) {
        return listing != null && listing.getOwner().getId().equals(userId);
    }

    // Check if the current user is the renter
    public boolean isRenter(UUID userId) {
        return renter != null && renter.getId().equals(userId);
    }

    // Check if user is involved in this booking
    public boolean isParticipant(UUID userId) {
        return isOwner(userId) || isRenter(userId);
    }
}
