package com.rentease.repository;

import com.rentease.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Find reviews for a listing
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.reviewer " +
            "WHERE r.listing.id = :listingId " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByListingId(@Param("listingId") UUID listingId, Pageable pageable);

    // Find reviews for a user (as reviewee/owner)
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.reviewer " +
            "JOIN FETCH r.listing " +
            "WHERE r.reviewee.id = :userId " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByRevieweeId(@Param("userId") UUID userId, Pageable pageable);

    // Find reviews written by a user
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.reviewee " +
            "JOIN FETCH r.listing " +
            "WHERE r.reviewer.id = :userId " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByReviewerId(@Param("userId") UUID userId, Pageable pageable);

    // Check if user already reviewed a booking
    boolean existsByBookingIdAndReviewerId(UUID bookingId, UUID reviewerId);

    // Find review by booking and reviewer
    Optional<Review> findByBookingIdAndReviewerId(UUID bookingId, UUID reviewerId);

    // Find review with all details
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.booking " +
            "JOIN FETCH r.listing " +
            "JOIN FETCH r.reviewer " +
            "JOIN FETCH r.reviewee " +
            "WHERE r.id = :id")
    Optional<Review> findByIdWithDetails(@Param("id") UUID id);

    // Get average rating for a listing
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.listing.id = :listingId")
    Double getAverageRatingForListing(@Param("listingId") UUID listingId);

    // Count reviews for a listing
    long countByListingId(UUID listingId);

    // Get rating distribution for a listing
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
            "WHERE r.listing.id = :listingId " +
            "GROUP BY r.rating " +
            "ORDER BY r.rating DESC")
    java.util.List<Object[]> getRatingDistribution(@Param("listingId") UUID listingId);
}
