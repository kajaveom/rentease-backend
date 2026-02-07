package com.rentease.controller;

import com.rentease.dto.response.ApiResponse;
import com.rentease.dto.response.ListingSummaryResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.security.CurrentUser;
import com.rentease.security.UserPrincipal;
import com.rentease.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{listingId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("listingId") UUID listingId) {
        favoriteService.addFavorite(currentUser.getId(), listingId);
        return ResponseEntity.ok(ApiResponse.success(null, "Added to favorites"));
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("listingId") UUID listingId) {
        favoriteService.removeFavorite(currentUser.getId(), listingId);
        return ResponseEntity.ok(ApiResponse.success(null, "Removed from favorites"));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ListingSummaryResponse>> getFavorites(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        PagedResponse<ListingSummaryResponse> response = favoriteService.getFavorites(
                currentUser.getId(), page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<Set<UUID>>> getFavoritedListingIds(
            @CurrentUser UserPrincipal currentUser) {
        Set<UUID> ids = favoriteService.getFavoritedListingIds(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(ids));
    }

    @GetMapping("/check/{listingId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("listingId") UUID listingId) {
        boolean isFavorited = favoriteService.isFavorited(currentUser.getId(), listingId);
        return ResponseEntity.ok(ApiResponse.success(isFavorited));
    }
}
