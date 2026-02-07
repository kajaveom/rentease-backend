package com.rentease.entity.enums;

public enum BookingStatus {
    REQUESTED,    // Renter has requested, waiting for owner approval
    APPROVED,     // Owner approved, waiting for rental period to start
    ACTIVE,       // Rental is currently active
    COMPLETED,    // Rental successfully completed
    CANCELLED,    // Cancelled by either party
    REJECTED      // Owner rejected the request
}
