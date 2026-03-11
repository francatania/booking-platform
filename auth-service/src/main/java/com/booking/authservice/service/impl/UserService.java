package com.booking.authservice.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.booking.authservice.entity.User;
import com.booking.authservice.model.dto.AuthResponse;
import com.booking.authservice.model.dto.LoginRequest;
import com.booking.authservice.model.dto.RegisterRequest;
import com.booking.authservice.model.dto.UserResponse;
import com.booking.authservice.model.enums.UserRole;
import com.booking.authservice.exception.InvalidCredentialsException;
import com.booking.authservice.exception.UserAlreadyExistsException;
import com.booking.authservice.repository.UserRepository;
import com.booking.authservice.service.interfaces.IJwtService;
import com.booking.authservice.service.interfaces.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class UserService implements IUserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final IJwtService jwtService;


    public UserResponse register(RegisterRequest dto, boolean isAdmin){

        if(repository.existsByEmail(dto.getEmail())){
            throw new UserAlreadyExistsException(dto.getEmail());
        }

        User user = User.builder()
                        .email(dto.getEmail())
                        .username(dto.getUsername())
                        .passwordHash(passwordEncoder.encode(dto.getPassword()))
                        .role(isAdmin ? UserRole.ADMIN : UserRole.USER)
                        .companyId(dto.getCompanyId())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        User saved = repository.save(user);
        return UserResponse.from(saved);
    }

    public AuthResponse login(LoginRequest dto){
        Optional<User> user = repository.findByUsername(dto.getUsername());
        if(user.isEmpty()){
            throw new InvalidCredentialsException();
        }

        if(!passwordEncoder.matches(dto.getPassword(), user.get().getPasswordHash())){
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.get());
        return AuthResponse.builder().token(token).build();

    }

}
