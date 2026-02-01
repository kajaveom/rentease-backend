package com.rentease.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message must be less than 2000 characters")
    private String content;

    // For starting a new conversation
    private UUID listingId;
}
