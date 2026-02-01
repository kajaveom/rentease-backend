package com.rentease.entity;

import com.rentease.entity.enums.Category;
import com.rentease.entity.enums.Condition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "price_per_day", nullable = false)
    private Integer pricePerDay; // Stored in cents

    @Column(name = "deposit_amount", nullable = false)
    private Integer depositAmount; // Stored in cents

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;

    @Column(length = 50)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(name = "pickup_location", nullable = false, length = 200)
    private String pickupLocation;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ListingImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addImage(ListingImage image) {
        images.add(image);
        image.setListing(this);
    }

    public void removeImage(ListingImage image) {
        images.remove(image);
        image.setListing(null);
    }

    public String getPrimaryImageUrl() {
        return images.stream()
                .filter(img -> img.getDisplayOrder() == 0)
                .findFirst()
                .map(ListingImage::getImageUrl)
                .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());
    }
}
