package com.rentease.service;

import com.rentease.dto.request.CreateReviewRequest;
import com.rentease.dto.request.ReviewResponseRequest;
import com.rentease.dto.response.PagedResponse;
import com.rentease.dto.response.ReviewResponse;
import com.rentease.entity.Booking;
import com.rentease.entity.Review;
import com.rentease.entity.enums.BookingStatus;
import com.rentease.exception.BadRequestException;
import com.rentease.exception.ForbiddenException;
import com.rentease.exception.ResourceNotFoundException;
import com.rentease.repository.BookingRepository;
import com.rentease.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public ReviewResponse createReview(UUID reviewerId, UUID bookingId, CreateReviewRequest request) {
        // Get the booking
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Verify the reviewer is the renter
        if (!booking.getRenter().getId().equals(reviewerId)) {
            throw new ForbiddenException("Only the renter can review this booking");
        }

        // Verify the booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("You can only review completed bookings");
        }

        // Check if already reviewed
        if (reviewRepository.existsByBookingIdAndReviewerId(bookingId, reviewerId)) {
            throw new BadRequestException("You have already reviewed this booking");
        }

        // Create the review
        Review review = Review.builder()
                .booking(booking)
                .listing(booking.getListing())
                .reviewer(booking.getRenter())
                .reviewee(booking.getListing().getOwner())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        return ReviewResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID reviewId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return ReviewResponse.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getListingReviews(UUID listingId, int page, int size) {
        Page<Review> reviews = reviewRepository.findByListingId(listingId, PageRequest.of(page, size));
        return mapToPagedResponse(reviews);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getUserReviews(UUID userId, int page, int size) {
        Page<Review> reviews = reviewRepository.findByRevieweeId(userId, PageRequest.of(page, size));
        return mapToPagedResponse(reviews);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getMyReviews(UUID userId, int page, int size) {
        Page<Review> reviews = reviewRepository.findByReviewerId(userId, PageRequest.of(page, size));
        return mapToPagedResponse(reviews);
    }

    @Transactional
    public ReviewResponse addOwnerResponse(UUID ownerId, UUID reviewId, ReviewResponseRequest request) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Verify the owner
        if (!review.getReviewee().getId().equals(ownerId)) {
            throw new ForbiddenException("Only the reviewed owner can respond");
        }

        // Check if already responded
        if (review.getOwnerResponse() != null) {
            throw new BadRequestException("You have already responded to this review");
        }

        review.setOwnerResponse(request.getResponse());
        review.setOwnerResponseAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return ReviewResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getListingReviewStats(UUID listingId) {
        Double averageRating = reviewRepository.getAverageRatingForListing(listingId);
        long totalReviews = reviewRepository.countByListingId(listingId);
        List<Object[]> distribution = reviewRepository.getRatingDistribution(listingId);

        Map<Integer, Long> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0L);
        }
        for (Object[] row : distribution) {
            ratingCounts.put((Integer) row[0], (Long) row[1]);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : null);
        stats.put("totalReviews", totalReviews);
        stats.put("ratingDistribution", ratingCounts);

        return stats;
    }

    @Transactional(readOnly = true)
    public boolean canReviewBooking(UUID userId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return false;

        // Must be the renter
        if (!booking.getRenter().getId().equals(userId)) return false;

        // Must be completed
        if (booking.getStatus() != BookingStatus.COMPLETED) return false;

        // Must not have reviewed already
        return !reviewRepository.existsByBookingIdAndReviewerId(bookingId, userId);
    }

    private PagedResponse<ReviewResponse> mapToPagedResponse(Page<Review> page) {
        List<ReviewResponse> content = page.getContent().stream()
                .map(ReviewResponse::fromEntity)
                .toList();

        return PagedResponse.<ReviewResponse>builder()
                .data(content)
                .pagination(PagedResponse.PaginationInfo.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }
}
