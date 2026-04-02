package com.booking.authservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotNull(message = "{username.required}")
    private String username;

    @NotNull(message = "{password.required}")
    private String password;
}