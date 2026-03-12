package com.booking.authservice.service;

import com.booking.authservice.entity.User;
import com.booking.authservice.model.enums.UserRole;
import com.booking.authservice.service.impl.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "mySecretKey1234567890mySecretKey1234567890";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .email("john@mail.com")
                .username("john")
                .role(UserRole.USER)
                .companyId(10L)
                .build();
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken(buildUser());
        assertThat(token).isNotBlank();
    }


    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtService.generateToken(buildUser());
        assertThat(jwtService.extractEmail(token)).isEqualTo("john@mail.com");
    }


    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtService.generateToken(buildUser());
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
    }

    @Test
    void isTokenValid_whenValidToken_returnsTrue() {
        String token = jwtService.generateToken(buildUser());
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_whenTamperedToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("this.is.invalid")).isFalse();
    }
}
