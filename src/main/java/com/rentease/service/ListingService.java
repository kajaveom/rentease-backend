package com.rentease.service;

import com.rentease.dto.request.CreateListingRequest;
import com.rentease.dto.request.UpdateListingRequest;
import com.rentease.dto.response.BookedDateRangeResponse;
import com.rentease.dto.response.ListingResponse;
import com.rentease.dto.response.ListingSummaryResponse;
import com.rentease.dto.response.PagedResponse;
import com.rentease.entity.Listing;
import com.rentease.entity.ListingImage;
import com.rentease.entity.User;
import com.rentease.entity.enums.Category;
import com.rentease.exception.ForbiddenException;
import com.rentease.exception.ResourceNotFoundException;
import com.rentease.mapper.ListingMapper;
import com.rentease.repository.BookingRepository;
import com.rentease.repository.ListingImageRepository;
import com.rentease.repository.ListingRepository;
import com.rentease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ListingMapper listingMapper;

    @Transactional
    @CacheEvict(value = "recentListings", allEntries = true)
    public ListingResponse createListing(UUID userId, CreateListingRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Listing listing = Listing.builder()
                .owner(owner)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .pricePerDay(request.getPricePerDay())
                .depositAmount(request.getDepositAmount())
                .condition(request.getCondition())
                .brand(request.getBrand())
                .model(request.getModel())
                .pickupLocation(request.getPickupLocation())
                .build();

        listing = listingRepository.save(listing);

        // Add images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ListingImage image = ListingImage.builder()
                        .listing(listing)
                        .imageUrl(request.getImageUrls().get(i))
                        .displayOrder(i)
                        .build();
                listing.addImage(image);
            }
            listing = listingRepository.save(listing);
        }

        log.info("Listing created: {} by user {}", listing.getId(), userId);

        return listingMapper.toResponse(listing);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "listing", key = "#listingId")
    public ListingResponse getListingById(UUID listingId) {
        Listing listing = listingRepository.findByIdAndActiveTrue(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        return listingMapper.toResponse(listing);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ListingSummaryResponse> getListings(
            String category,
            String query,
            Integer minPrice,
            Integer maxPrice,
            String sort,
            int page,
            int size
    ) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Listing> listingPage;

        Category categoryEnum = null;
        if (category != null && !category.isEmpty()) {
            try {
                categoryEnum = Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid category, ignore filter
            }
        }

        boolean hasQuery = query != null && !query.trim().isEmpty();
        boolean useFullTextSearch = hasQuery && query.trim().length() >= 3;

        if (categoryEnum != null && hasQuery) {
            if (useFullTextSearch) {
                // Use PostgreSQL full-text search for queries 3+ characters
                listingPage = listingRepository.searchByCategoryAndQuery(categoryEnum.name(), query.trim(), pageable);
            } else {
                // Fallback to LIKE search for short queries
                listingPage = listingRepository.searchByCategoryAndQueryFallback(categoryEnum, query.trim(), pageable);
            }
        } else if (categoryEnum != null) {
            listingPage = listingRepository.findByCategory(categoryEnum, pageable);
        } else if (hasQuery) {
            if (useFullTextSearch) {
                // Use PostgreSQL full-text search for queries 3+ characters
                listingPage = listingRepository.searchByQuery(query.trim(), pageable);
            } else {
                // Fallback to LIKE search for short queries
                listingPage = listingRepository.searchByQueryFallback(query.trim(), pageable);
            }
        } else {
            listingPage = listingRepository.findAllActive(pageable);
        }

        List<ListingSummaryResponse> listings = listingPage.getContent().stream()
                .map(listingMapper::toSummaryResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                listings,
                page,
                size,
                listingPage.getTotalElements(),
                listingPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<ListingSummaryResponse> getMyListings(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Listing> listingPage = listingRepository.findByOwnerIdAndActiveTrue(userId, pageable);

        List<ListingSummaryResponse> listings = listingPage.getContent().stream()
                .map(listingMapper::toSummaryResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                listings,
                page,
                size,
                listingPage.getTotalElements(),
                listingPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<ListingSummaryResponse> getUserListings(UUID userId, int page, int size) {
        // For public view, only show available listings
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Listing> listingPage = listingRepository.findByOwnerIdAndActiveTrueAndAvailableTrue(userId, pageable);

        List<ListingSummaryResponse> listings = listingPage.getContent().stream()
                .map(listingMapper::toSummaryResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                listings,
                page,
                size,
                listingPage.getTotalElements(),
                listingPage.getTotalPages()
        );
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listing", key = "#listingId"),
            @CacheEvict(value = "recentListings", allEntries = true)
    })
    public ListingResponse updateListing(UUID userId, UUID listingId, UpdateListingRequest request) {
        Listing listing = listingRepository.findByIdAndActiveTrue(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        if (!listing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to update this listing");
        }

        if (request.getTitle() != null) listing.setTitle(request.getTitle());
        if (request.getDescription() != null) listing.setDescription(request.getDescription());
        if (request.getCategory() != null) listing.setCategory(request.getCategory());
        if (request.getPricePerDay() != null) listing.setPricePerDay(request.getPricePerDay());
        if (request.getDepositAmount() != null) listing.setDepositAmount(request.getDepositAmount());
        if (request.getCondition() != null) listing.setCondition(request.getCondition());
        if (request.getBrand() != null) listing.setBrand(request.getBrand());
        if (request.getModel() != null) listing.setModel(request.getModel());
        if (request.getPickupLocation() != null) listing.setPickupLocation(request.getPickupLocation());
        if (request.getAvailable() != null) listing.setAvailable(request.getAvailable());

        // Update images if provided (replace all existing images)
        if (request.getImageUrls() != null) {
            // Clear existing images
            listing.getImages().clear();

            // Add new images
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ListingImage image = ListingImage.builder()
                        .listing(listing)
                        .imageUrl(request.getImageUrls().get(i))
                        .displayOrder(i)
                        .build();
                listing.addImage(image);
            }
        }

        listing = listingRepository.save(listing);
        log.info("Listing updated: {}", listingId);

        return listingMapper.toResponse(listing);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "listing", key = "#listingId"),
            @CacheEvict(value = "recentListings", allEntries = true)
    })
    public void deleteListing(UUID userId, UUID listingId) {
        Listing listing = listingRepository.findByIdAndActiveTrue(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        if (!listing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to delete this listing");
        }

        // Soft delete
        listing.setActive(false);
        listingRepository.save(listing);
        log.info("Listing soft-deleted: {}", listingId);
    }

    @Transactional
    @CacheEvict(value = "listing", key = "#listingId")
    public ListingResponse addImage(UUID userId, UUID listingId, String imageUrl, String publicId) {
        Listing listing = listingRepository.findByIdAndActiveTrue(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        if (!listing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to modify this listing");
        }

        int nextOrder = listingImageRepository.findMaxDisplayOrderByListingId(listingId)
                .orElse(-1) + 1;

        ListingImage image = ListingImage.builder()
                .listing(listing)
                .imageUrl(imageUrl)
                .publicId(publicId)
                .displayOrder(nextOrder)
                .build();

        listing.addImage(image);
        listing = listingRepository.save(listing);

        log.info("Image added to listing: {}", listingId);
        return listingMapper.toResponse(listing);
    }

    @Transactional
    @CacheEvict(value = "listing", key = "#listingId")
    public void deleteImage(UUID userId, UUID listingId, UUID imageId) {
        Listing listing = listingRepository.findByIdAndActiveTrue(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        if (!listing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to modify this listing");
        }

        ListingImage image = listing.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        listing.removeImage(image);
        listingImageRepository.delete(image);

        log.info("Image {} removed from listing {}", imageId, listingId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "recentListings", key = "#limit")
    public List<ListingSummaryResponse> getRecentListings(int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 20));
        List<Listing> listings = listingRepository.findRecentListings(pageable);
        return listings.stream()
                .map(listingMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookedDateRangeResponse> getBookedDates(UUID listingId) {
        // Verify listing exists
        listingRepository.findByIdAndActiveTrue(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

        // Get active bookings from today onwards
        return bookingRepository.findActiveBookingsForListing(listingId, LocalDate.now())
                .stream()
                .map(booking -> BookedDateRangeResponse.builder()
                        .startDate(booking.getStartDate())
                        .endDate(booking.getEndDate())
                        .build())
                .collect(Collectors.toList());
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");

        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "price_asc" -> sortOrder = Sort.by(Sort.Direction.ASC, "pricePerDay");
                case "price_desc" -> sortOrder = Sort.by(Sort.Direction.DESC, "pricePerDay");
                case "newest" -> sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");
                case "oldest" -> sortOrder = Sort.by(Sort.Direction.ASC, "createdAt");
            }
        }

        return PageRequest.of(page, Math.min(size, 50), sortOrder);
    }
}
