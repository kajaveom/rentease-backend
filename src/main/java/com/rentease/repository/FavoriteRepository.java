package com.rentease.repository;

import com.rentease.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByUserIdAndListingId(UUID userId, UUID listingId);

    boolean existsByUserIdAndListingId(UUID userId, UUID listingId);

    void deleteByUserIdAndListingId(UUID userId, UUID listingId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.listing l WHERE f.user.id = :userId AND l.active = true ORDER BY f.createdAt DESC")
    Page<Favorite> findByUserIdWithListing(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT f.listing.id FROM Favorite f WHERE f.user.id = :userId")
    List<UUID> findListingIdsByUserId(@Param("userId") UUID userId);

    long countByListingId(UUID listingId);
}
