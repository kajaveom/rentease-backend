package com.rentease.repository;

import com.rentease.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, UUID> {

    List<ListingImage> findByListingIdOrderByDisplayOrderAsc(UUID listingId);

    @Query("SELECT MAX(li.displayOrder) FROM ListingImage li WHERE li.listing.id = :listingId")
    Optional<Integer> findMaxDisplayOrderByListingId(@Param("listingId") UUID listingId);

    void deleteByListingId(UUID listingId);
}
