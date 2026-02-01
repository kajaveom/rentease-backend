package com.rentease.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookingActionRequest {

    @Size(max = 500, message = "Response must be less than 500 characters")
    private String response;

    @Size(max = 500, message = "Cancellation reason must be less than 500 characters")
    private String cancellationReason;
}
