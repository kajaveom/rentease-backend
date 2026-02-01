package com.rentease.controller;

import com.rentease.dto.request.CreateReviewRequest;
import com.rentease.dto.request.ReviewResponseRequest;
import com.rentease.dto.response.ApiResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.dto.response.ReviewResponse;
import com.rentease.security.CurrentUser;
import com.rentease.security.UserPrincipal;
import com.rentease.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Create a review for a booking
    @PostMapping("/bookings/{bookingId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID bookingId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(currentUser.getId(), bookingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Get a specific review
    @GetMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(@PathVariable UUID id) {
        ReviewResponse response = reviewService.getReview(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Get reviews for a listing
    @GetMapping("/listings/{listingId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> getListingReviews(
            @PathVariable UUID listingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<ReviewResponse> response = reviewService.getListingReviews(listingId, page, size);
        return ResponseEntity.ok(response);
    }

    // Get review stats for a listing
    @GetMapping("/listings/{listingId}/reviews/stats")
    public ResponseEntity<Map<String, Object>> getListingReviewStats(@PathVariable UUID listingId) {
        Map<String, Object> stats = reviewService.getListingReviewStats(listingId);
        return ResponseEntity.ok(stats);
    }

    // Get reviews for a user (as reviewee)
    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> getUserReviews(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<ReviewResponse> response = reviewService.getUserReviews(userId, page, size);
        return ResponseEntity.ok(response);
    }

    // Get reviews written by current user
    @GetMapping("/users/me/reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> getMyReviews(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<ReviewResponse> response = reviewService.getMyReviews(currentUser.getId(), page, size);
        return ResponseEntity.ok(response);
    }

    // Add owner response to a review
    @PostMapping("/reviews/{id}/response")
    public ResponseEntity<ApiResponse<ReviewResponse>> addOwnerResponse(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ReviewResponseRequest request
    ) {
        ReviewResponse response = reviewService.addOwnerResponse(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Check if user can review a booking
    @GetMapping("/bookings/{bookingId}/can-review")
    public ResponseEntity<Map<String, Boolean>> canReviewBooking(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID bookingId
    ) {
        boolean canReview = reviewService.canReviewBooking(currentUser.getId(), bookingId);
        return ResponseEntity.ok(Map.of("canReview", canReview));
    }
}
