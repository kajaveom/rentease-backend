package com.rentease.entity.enums;

public enum BookingStatus {
    REQUESTED,    // Renter has requested, waiting for owner approval
    APPROVED,     // Owner approved, waiting for payment
    PAID,         // Payment received, waiting for rental period
    ACTIVE,       // Rental is currently active
    COMPLETED,    // Rental successfully completed
    CANCELLED,    // Cancelled by either party
    REJECTED      // Owner rejected the request
}
