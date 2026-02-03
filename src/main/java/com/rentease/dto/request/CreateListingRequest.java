package com.rentease.dto.request;

import com.rentease.entity.enums.Category;
import com.rentease.entity.enums.Condition;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateListingRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Price per day is required")
    @Min(value = 100, message = "Price must be at least $1.00")
    private Integer pricePerDay;

    @NotNull(message = "Deposit amount is required")
    @Min(value = 0, message = "Deposit cannot be negative")
    private Integer depositAmount;

    @NotNull(message = "Condition is required")
    private Condition condition;

    @Size(max = 50, message = "Brand must be less than 50 characters")
    private String brand;

    @Size(max = 100, message = "Model must be less than 100 characters")
    private String model;

    @NotBlank(message = "Pickup location is required")
    @Size(max = 200, message = "Pickup location must be less than 200 characters")
    private String pickupLocation;

    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageUrls;
}
