package com.rentease.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {

    @NotBlank(message = "Google credential is required")
    private String credential; // The ID token from Google Sign-In
}
