package com.rentease.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdVerificationRequest {

    @NotBlank(message = "Document URL is required")
    private String documentUrl;

    private String documentType; // e.g., "passport", "drivers_license", "national_id"
}
