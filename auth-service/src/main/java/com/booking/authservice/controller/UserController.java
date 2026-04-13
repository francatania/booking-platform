package com.booking.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booking.authservice.model.dto.AuthResponse;
import com.booking.authservice.model.dto.LoginRequest;
import com.booking.authservice.model.dto.RegisterRequest;
import com.booking.authservice.model.dto.UserResponse;
import com.booking.authservice.service.interfaces.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Registration and login")
public class UserController {
    private final IUserService service;

    @Operation(summary = "Register a new admin user (requires SUPER_ADMIN)")
    @PostMapping("/register-admin")
    public ResponseEntity<UserResponse> registerAdmin(@RequestBody RegisterRequest dto){
        UserResponse response = service.register(dto, true);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest dto){
        UserResponse response = service.register(dto, false);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login and obtain JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest dto){
        AuthResponse response = service.login(dto);
        return ResponseEntity.ok(response);
    }

}
