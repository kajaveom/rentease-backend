package com.rentease.repository;

import com.rentease.entity.Booking;
import com.rentease.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Find bookings where user is the renter
    @Query("SELECT b FROM Booking b WHERE b.renter.id = :renterId ORDER BY b.createdAt DESC")
    Page<Booking> findByRenterId(@Param("renterId") UUID renterId, Pageable pageable);

    // Find bookings for listings owned by user
    @Query("SELECT b FROM Booking b WHERE b.listing.owner.id = :ownerId ORDER BY b.createdAt DESC")
    Page<Booking> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    // Find bookings for a specific listing
    Page<Booking> findByListingId(UUID listingId, Pageable pageable);

    // Find booking with all related entities loaded
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.listing l " +
            "JOIN FETCH l.owner " +
            "JOIN FETCH b.renter " +
            "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") UUID id);

    // Check for overlapping bookings (for validation)
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.listing.id = :listingId " +
            "AND b.status IN :statuses " +
            "AND b.startDate <= :endDate " +
            "AND b.endDate >= :startDate")
    boolean hasOverlappingBookings(
            @Param("listingId") UUID listingId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<BookingStatus> statuses
    );

    // Find bookings by status for a renter
    @Query("SELECT b FROM Booking b WHERE b.renter.id = :renterId AND b.status = :status ORDER BY b.createdAt DESC")
    Page<Booking> findByRenterIdAndStatus(
            @Param("renterId") UUID renterId,
            @Param("status") BookingStatus status,
            Pageable pageable
    );

    // Find bookings by status for an owner
    @Query("SELECT b FROM Booking b WHERE b.listing.owner.id = :ownerId AND b.status = :status ORDER BY b.createdAt DESC")
    Page<Booking> findByOwnerIdAndStatus(
            @Param("ownerId") UUID ownerId,
            @Param("status") BookingStatus status,
            Pageable pageable
    );

    // Get booked dates for a listing (for calendar display)
    @Query("SELECT b FROM Booking b " +
            "WHERE b.listing.id = :listingId " +
            "AND b.status IN ('APPROVED', 'PAID', 'ACTIVE') " +
            "AND b.endDate >= :fromDate")
    List<Booking> findActiveBookingsForListing(
            @Param("listingId") UUID listingId,
            @Param("fromDate") LocalDate fromDate
    );

    // Count pending requests for owner (for notifications)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.listing.owner.id = :ownerId AND b.status = 'REQUESTED'")
    long countPendingRequestsForOwner(@Param("ownerId") UUID ownerId);
}
