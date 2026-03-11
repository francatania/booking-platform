package com.booking.companyservice.service.interfaces;

import io.jsonwebtoken.Claims;

public interface IJwtService {

    Claims extractClaims(String token);

    String extractEmail(String token);

    String extractRole(String token);

    Long extractCompanyId(String token);

    boolean isTokenValid(String token);
}
