package com.booking.companyservice.config;

public record UserPrincipal(String email, String role, Long companyId) {}
