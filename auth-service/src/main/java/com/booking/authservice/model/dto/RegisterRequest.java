package com.booking.authservice.model.dto;

import com.booking.authservice.model.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RegisterRequest {

    @Max(value = 20, message = "Username can't have more than 20 characters.")
    private String username;

    private String firstName;

    private String lastName;

    @Email
    private String email;

    @Min(value = 6, message = "Password must have more than 5 characters.")
    private String password;

    private Long companyId;

    private UserRole role;
}