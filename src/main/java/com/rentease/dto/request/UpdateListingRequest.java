package com.rentease.dto.request;

import com.rentease.entity.enums.Category;
import com.rentease.entity.enums.Condition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateListingRequest {

    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    private Category category;

    @Min(value = 100, message = "Price must be at least $1.00")
    private Integer pricePerDay;

    @Min(value = 0, message = "Deposit cannot be negative")
    private Integer depositAmount;

    private Condition condition;

    @Size(max = 50, message = "Brand must be less than 50 characters")
    private String brand;

    @Size(max = 100, message = "Model must be less than 100 characters")
    private String model;

    @Size(max = 200, message = "Pickup location must be less than 200 characters")
    private String pickupLocation;

    private Boolean available;
}
