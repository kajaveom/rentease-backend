package com.rentease.entity;

import com.rentease.entity.enums.IdVerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String location;

    @Column(name = "id_verified", nullable = false)
    @Builder.Default
    private boolean idVerified = false;

    @Column(name = "id_document_url", length = 500)
    private String idDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_verification_status", nullable = false, length = 20)
    @Builder.Default
    private IdVerificationStatus idVerificationStatus = IdVerificationStatus.NONE;

    @Column(name = "id_verification_submitted_at")
    private LocalDateTime idVerificationSubmittedAt;

    @Column(name = "id_verification_rejection_reason", columnDefinition = "TEXT")
    private String idVerificationRejectionReason;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPublicLastName() {
        return lastName != null && !lastName.isEmpty() ? lastName.charAt(0) + "." : "";
    }
}
