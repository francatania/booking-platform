package com.booking.authservice.service.interfaces;

import com.booking.authservice.model.dto.AuthResponse;
import com.booking.authservice.model.dto.LoginRequest;
import com.booking.authservice.model.dto.RegisterRequest;
import com.booking.authservice.model.dto.UserResponse;

/**
 * Service contract for user registration and authentication.
 */
public interface IUserService {

    /**
     * Registers a new user in the system.
     *
     * @param dto       the registration data (username, email, password, role, etc.)
     * @param isNotUser {@code true} if registering with an elevated role (ADMIN, OPERATOR);
     *                  {@code false} for regular USER accounts
     * @return a {@link UserResponse} with the created user's public data
     * @throws com.booking.authservice.exception.UserAlreadyExistsException if the username or email is already taken
     */
    UserResponse register(RegisterRequest dto, boolean isNotUser);

    /**
     * Authenticates a user and issues a signed JWT token.
     *
     * @param dto the login credentials (username and password)
     * @return an {@link AuthResponse} containing the JWT token
     * @throws com.booking.authservice.exception.InvalidCredentialsException if the credentials are invalid
     */
    AuthResponse login(LoginRequest dto);
}
