package com.booking.companyservice.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCompanyServiceRequest {

    @NotBlank(message = "{name.required}")
    private String name;

    private String description;

    @NotNull(message = "{duration.required}")
    @Min(value = 1, message = "{duration.min}")
    private Integer durationMinutes;

    @NotNull(message = "{price.required}")
    @Min(value = 0, message = "{price.min}")
    private BigDecimal price;
}
