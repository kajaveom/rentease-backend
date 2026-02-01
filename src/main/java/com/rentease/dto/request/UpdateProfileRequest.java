package com.rentease.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Size(max = 500, message = "Bio must be less than 500 characters")
    private String bio;

    @Size(max = 100, message = "Location must be less than 100 characters")
    private String location;

    @Size(max = 500, message = "Avatar URL must be less than 500 characters")
    private String avatarUrl;
}
