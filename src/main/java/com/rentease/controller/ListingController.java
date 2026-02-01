package com.rentease.controller;

import com.rentease.dto.request.CreateListingRequest;
import com.rentease.dto.request.UpdateListingRequest;
import com.rentease.dto.response.ApiResponse;
import com.rentease.dto.response.ListingResponse;
import com.rentease.dto.response.ListingSummaryResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.security.CurrentUser;
import com.rentease.security.UserPrincipal;
import com.rentease.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateListingRequest request
    ) {
        ListingResponse response = listingService.createListing(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ListingSummaryResponse>> getListings(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        PagedResponse<ListingSummaryResponse> response = listingService.getListings(
                category, q, minPrice, maxPrice, sort, page, size
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> getListing(@PathVariable UUID id) {
        ListingResponse response = listingService.getListingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateListingRequest request
    ) {
        ListingResponse response = listingService.updateListing(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        listingService.deleteListing(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ListingResponse>> addImage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @RequestParam String imageUrl,
            @RequestParam String publicId
    ) {
        ListingResponse response = listingService.addImage(currentUser.getId(), id, imageUrl, publicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{listingId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID listingId,
            @PathVariable UUID imageId
    ) {
        listingService.deleteImage(currentUser.getId(), listingId, imageId);
        return ResponseEntity.noContent().build();
    }
}
