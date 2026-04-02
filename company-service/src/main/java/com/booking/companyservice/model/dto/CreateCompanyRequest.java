package com.booking.companyservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCompanyRequest {

    @NotBlank(message = "{name.required}")
    private String name;

    private String description;

    @NotBlank(message = "{address.required}")
    private String address;

    @NotBlank(message = "{phone.required}")
    private String phone;
}
