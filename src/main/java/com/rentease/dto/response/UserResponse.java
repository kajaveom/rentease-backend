package com.rentease.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String bio;
    private String location;
    private boolean idVerified;
    private boolean emailVerified;
    private Double averageRating;
    private Integer totalReviews;
    private LocalDateTime createdAt;
}
