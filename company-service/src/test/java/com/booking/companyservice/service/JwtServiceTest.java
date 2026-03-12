package com.booking.companyservice.service;

import com.booking.companyservice.service.impl.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2";
    private static final String EMAIL = "user@test.com";
    private static final String ROLE = "ADMIN";
    private static final Long COMPANY_ID = 1L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    private String buildToken(String email, String role, Long companyId, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("role", role, "companyId", companyId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = buildToken(EMAIL, ROLE, COMPANY_ID, 86400000L);

        assertEquals(EMAIL, jwtService.extractEmail(token));
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = buildToken(EMAIL, ROLE, COMPANY_ID, 86400000L);

        assertEquals(ROLE, jwtService.extractRole(token));
    }

    @Test
    void extractCompanyId_returnsCorrectCompanyId() {
        String token = buildToken(EMAIL, ROLE, COMPANY_ID, 86400000L);

        assertEquals(COMPANY_ID, jwtService.extractCompanyId(token));
    }

    @Test
    void isTokenValid_whenValidToken_returnsTrue() {
        String token = buildToken(EMAIL, ROLE, COMPANY_ID, 86400000L);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_whenExpiredToken_returnsFalse() {
        String token = buildToken(EMAIL, ROLE, COMPANY_ID, -1000L);

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_whenTamperedToken_returnsFalse() {
        String token = buildToken(EMAIL, ROLE, COMPANY_ID, 86400000L) + "tampered";

        assertFalse(jwtService.isTokenValid(token));
    }

}
