package com.booking.companyservice.service.interfaces;

import io.jsonwebtoken.Claims;

/**
 * Service contract for JWT token parsing and validation.
 */
public interface IJwtService {

    /**
     * Parses and returns all claims from a JWT token.
     *
     * @param token the JWT string
     * @return the {@link Claims} payload
     */
    Claims extractClaims(String token);

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
     * @return the role claim value (e.g. "USER", "ADMIN", "OPERATOR")
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
