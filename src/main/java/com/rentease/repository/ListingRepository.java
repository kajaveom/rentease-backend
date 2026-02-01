package com.rentease.repository;

import com.rentease.entity.Listing;
import com.rentease.entity.enums.Category;
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
public interface ListingRepository extends JpaRepository<Listing, UUID> {

    @Query("SELECT l FROM Listing l WHERE l.active = true AND l.available = true ORDER BY l.createdAt DESC")
    Page<Listing> findAllActive(Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.active = true AND l.available = true AND l.category = :category ORDER BY l.createdAt DESC")
    Page<Listing> findByCategory(@Param("category") Category category, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.active = true AND l.available = true " +
           "AND (LOWER(l.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(l.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY l.createdAt DESC")
    Page<Listing> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.active = true AND l.available = true " +
           "AND l.category = :category " +
           "AND (LOWER(l.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(l.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY l.createdAt DESC")
    Page<Listing> searchByCategoryAndQuery(@Param("category") Category category, @Param("query") String query, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.active = true AND l.available = true " +
           "AND l.pricePerDay >= :minPrice AND l.pricePerDay <= :maxPrice " +
           "ORDER BY l.createdAt DESC")
    Page<Listing> findByPriceRange(@Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice, Pageable pageable);

    List<Listing> findByOwnerIdAndActiveTrue(UUID ownerId);

    Page<Listing> findByOwnerIdAndActiveTrue(UUID ownerId, Pageable pageable);

    Page<Listing> findByOwnerIdAndActiveTrueAndAvailableTrue(UUID ownerId, Pageable pageable);

    Optional<Listing> findByIdAndActiveTrue(UUID id);

    @Query("SELECT COUNT(l) FROM Listing l WHERE l.owner.id = :ownerId AND l.active = true")
    long countByOwnerId(@Param("ownerId") UUID ownerId);
}
