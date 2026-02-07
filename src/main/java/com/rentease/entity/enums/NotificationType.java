package com.rentease.entity.enums;

public enum NotificationType {
    // Booking notifications
    BOOKING_REQUESTED, // Owner receives when someone requests to book their item
    BOOKING_APPROVED, // Renter receives when owner approves their booking
    BOOKING_REJECTED, // Renter receives when owner rejects their booking
    BOOKING_CANCELLED, // Both parties receive when booking is cancelled
    BOOKING_PAID, // Owner receives when renter pays
    BOOKING_STARTED, // Renter receives when rental period starts
    BOOKING_COMPLETED, // Both parties receive when rental is completed

    // Review notifications
    REVIEW_RECEIVED, // User receives when they get a review

    // Message notifications
    NEW_MESSAGE // User receives when they get a new message
}
