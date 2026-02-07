package com.rentease.service;

import com.rentease.dto.response.ListingSummaryResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.entity.Favorite;
import com.rentease.entity.Listing;
import com.rentease.entity.User;
import com.rentease.exception.BadRequestException;
import com.rentease.exception.ResourceNotFoundException;
import com.rentease.mapper.ListingMapper;
import com.rentease.repository.FavoriteRepository;
import com.rentease.repository.ListingRepository;
import com.rentease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingMapper listingMapper;

    @Transactional
    public void addFavorite(UUID userId, UUID listingId) {
        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndListingId(userId, listingId)) {
            throw new BadRequestException("Listing is already in your favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findByIdAndActiveTrue(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        // Prevent favoriting own listings
        if (listing.getOwner().getId().equals(userId)) {
            throw new BadRequestException("You cannot favorite your own listing");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .listing(listing)
                .build();

        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID listingId) {
        if (!favoriteRepository.existsByUserIdAndListingId(userId, listingId)) {
            throw new ResourceNotFoundException("Favorite not found");
        }

        favoriteRepository.deleteByUserIdAndListingId(userId, listingId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ListingSummaryResponse> getFavorites(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Favorite> favoritesPage = favoriteRepository.findByUserIdWithListing(userId, pageable);

        List<ListingSummaryResponse> listings = favoritesPage.getContent().stream()
                .map(fav -> listingMapper.toSummaryResponse(fav.getListing()))
                .collect(Collectors.toList());

        return PagedResponse.of(
                listings,
                favoritesPage.getNumber(),
                favoritesPage.getSize(),
                favoritesPage.getTotalElements(),
                favoritesPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(UUID userId, UUID listingId) {
        return favoriteRepository.existsByUserIdAndListingId(userId, listingId);
    }

    @Transactional(readOnly = true)
    public Set<UUID> getFavoritedListingIds(UUID userId) {
        return favoriteRepository.findListingIdsByUserId(userId)
                .stream()
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public long getFavoriteCount(UUID listingId) {
        return favoriteRepository.countByListingId(listingId);
    }
}
