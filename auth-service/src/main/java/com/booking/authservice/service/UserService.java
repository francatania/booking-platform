package com.booking.authservice.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.booking.authservice.entity.User;
import com.booking.authservice.model.dto.RegisterRequest;
import com.booking.authservice.model.dto.UserResponse;
import com.booking.authservice.model.enums.UserRole;
import com.booking.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class UserService {
    
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;



    public UserResponse register(RegisterRequest dto){

        if(repository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
        .email(dto.getEmail())
        .username(dto.getUsername())
        .passwordHash(passwordEncoder.encode(dto.getPassword()))
        .role(UserRole.USER)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

        User saved = repository.save(user);
        return UserResponse.from(saved);
    }

}
