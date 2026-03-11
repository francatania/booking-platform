package com.booking.authservice.service.interfaces;

import com.booking.authservice.entity.User;

public interface IJwtService {

    String generateToken(User user);

    String extractEmail(String token);

    String extractRole(String token);

    Long extractCompanyId(String token);

    boolean isTokenValid(String token);
}
