package com.booking.companyservice.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCompanyServiceRequest {

    private String name;

    private String description;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @Min(value = 0, message = "Price must be positive")
    private BigDecimal price;

    private Boolean isActive;
}
