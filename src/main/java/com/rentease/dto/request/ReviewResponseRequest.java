package com.rentease.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewResponseRequest {

    @NotBlank(message = "Response is required")
    @Size(max = 500, message = "Response must be less than 500 characters")
    private String response;
}
