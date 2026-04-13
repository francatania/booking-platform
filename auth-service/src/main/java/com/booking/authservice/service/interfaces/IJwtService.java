package com.booking.authservice.service.interfaces;

import com.booking.authservice.entity.User;

/**
 * Service contract for JWT token generation and validation.
 */
public interface IJwtService {

    /**
     * Generates a signed JWT token for the given user.
     *
     * @param user the authenticated user entity
     * @return a signed JWT string
     */
    String generateToken(User user);

    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token the JWT string
     * @return the email stored in the token's subject claim
     */
    String extractEmail(String token);

    /**
     * Extracts the user's role from a JWT token.
     *
     * @param token the JWT string
     * @return the role claim value (e.g. "USER", "ADMIN")
     */
    String extractRole(String token);

    /**
     * Extracts the company ID from a JWT token.
     *
     * @param token the JWT string
     * @return the company ID, or {@code null} if the user has no associated company
     */
    Long extractCompanyId(String token);

    /**
     * Validates a JWT token's signature and expiration.
     *
     * @param token the JWT string to validate
     * @return {@code true} if the token is valid and not expired; {@code false} otherwise
     */
    boolean isTokenValid(String token);
}
