package com.rentease.controller;

import com.rentease.dto.request.ChangePasswordRequest;
import com.rentease.dto.request.IdVerificationRequest;
import com.rentease.dto.request.UpdateProfileRequest;
import com.rentease.dto.response.*;
import com.rentease.security.CurrentUser;
import com.rentease.security.UserPrincipal;
import com.rentease.service.ListingService;
import com.rentease.service.ReviewService;
import com.rentease.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ListingService listingService;
    private final ReviewService reviewService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        UserResponse response = userService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/listings")
    public ResponseEntity<PagedResponse<ListingSummaryResponse>> getMyListings(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        PagedResponse<ListingSummaryResponse> response = listingService.getMyListings(
                currentUser.getId(), page, size
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> getUser(@PathVariable UUID id) {
        PublicProfileResponse response = userService.getPublicProfile(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/listings")
    public ResponseEntity<PagedResponse<ListingSummaryResponse>> getUserListings(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        PagedResponse<ListingSummaryResponse> response = listingService.getUserListings(id, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> getUserReviews(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        PagedResponse<ReviewResponse> response = reviewService.getUserReviews(id, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@CurrentUser UserPrincipal currentUser) {
        userService.deleteAccount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
    }

    @GetMapping("/me/verification")
    public ResponseEntity<ApiResponse<IdVerificationResponse>> getVerificationStatus(
            @CurrentUser UserPrincipal currentUser
    ) {
        IdVerificationResponse response = userService.getVerificationStatus(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/me/verification")
    public ResponseEntity<ApiResponse<IdVerificationResponse>> submitVerification(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody IdVerificationRequest request
    ) {
        IdVerificationResponse response = userService.submitVerification(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Verification submitted successfully"));
    }

    @DeleteMapping("/me/verification")
    public ResponseEntity<ApiResponse<IdVerificationResponse>> cancelVerification(
            @CurrentUser UserPrincipal currentUser
    ) {
        IdVerificationResponse response = userService.cancelVerification(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Verification cancelled"));
    }
}
