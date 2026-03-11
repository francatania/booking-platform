package com.booking.authservice.service.interfaces;

import com.booking.authservice.model.dto.AuthResponse;
import com.booking.authservice.model.dto.LoginRequest;
import com.booking.authservice.model.dto.RegisterRequest;
import com.booking.authservice.model.dto.UserResponse;

public interface IUserService {

    UserResponse register(RegisterRequest dto, boolean isAdmin);

    AuthResponse login(LoginRequest dto);
}
