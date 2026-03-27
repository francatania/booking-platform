package com.booking.authservice.service;

import com.booking.authservice.entity.User;
import com.booking.authservice.exception.InvalidCredentialsException;
import com.booking.authservice.exception.UserAlreadyExistsException;
import com.booking.authservice.model.dto.AuthResponse;
import com.booking.authservice.model.dto.LoginRequest;
import com.booking.authservice.model.dto.RegisterRequest;
import com.booking.authservice.model.dto.UserResponse;
import com.booking.authservice.model.enums.UserRole;
import com.booking.authservice.repository.UserRepository;
import com.booking.authservice.service.impl.UserService;
import com.booking.authservice.service.interfaces.IJwtService;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IJwtService jwtService;

    @InjectMocks
    private UserService userService;


    private RegisterRequest buildRegisterDto() {
        RegisterRequest dto = new RegisterRequest();
        dto.setEmail("example@gmail.com");
        dto.setUsername("john");
        dto.setPassword("pass123");
        dto.setCompanyId(1L);
        return dto;
    }

    private LoginRequest buildLoginDto() {
        LoginRequest dto = new LoginRequest();
        dto.setUsername("john");
        dto.setPassword("pass123");
        return dto;
    }



    @Test
    void register_userAlreadyExisting(){
        RegisterRequest dto = this.buildRegisterDto();
        when(repository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(dto, false));

    }

    @Test
    void register_newUser(){
        RegisterRequest dto = this.buildRegisterDto();
        User saved = User.builder()
            .email(dto.getEmail())
            .username(dto.getUsername())
            .role(UserRole.USER)
            .companyId(dto.getCompanyId())
            .build();

        when(repository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(dto, false);

        assertEquals(saved.getEmail(), response.getEmail());
        assertEquals(saved.getCompanyId(), response.getCompanyId());
        assertEquals(saved.getRole(), response.getRole());
        assertEquals(saved.getUsername(), response.getUsername());
    }

    @Test
    void register_newAdmin(){
        RegisterRequest dto = this.buildRegisterDto();
        dto.setRole(UserRole.ADMIN);
        User saved = User.builder()
            .email(dto.getEmail())
            .username(dto.getUsername())
            .role(UserRole.USER)
            .companyId(dto.getCompanyId())
            .build();

        when(repository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(dto, true);

        assertEquals(saved.getEmail(), response.getEmail());
        assertEquals(saved.getCompanyId(), response.getCompanyId());
        assertEquals(saved.getRole(), response.getRole());
        assertEquals(saved.getUsername(), response.getUsername());
    }

    @Test
    void login_invalidUsername(){
        LoginRequest loginDto = this.buildLoginDto();
        when(repository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,()-> userService.login(loginDto));
    }

    @Test
    void login_invalidPassword(){
        LoginRequest loginDto = this.buildLoginDto();
        User user = User.builder()
                    .username("john")
                    .passwordHash("hash")
                    .build();
        when(repository.findByUsername(loginDto.getUsername()))
                .thenReturn(Optional.of(user));            

        when(passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,()-> userService.login(loginDto));
    }

    @Test
    void login_successful(){
        LoginRequest loginDto = this.buildLoginDto();
        User user = User.builder()
                    .username("john")
                    .passwordHash("hash")
                    .build();

        when(repository.findByUsername(loginDto.getUsername()))
                .thenReturn(Optional.of(user)); 

        when(passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(true);

        when(jwtService.generateToken(user))
            .thenReturn("token12345");
        
        AuthResponse response = userService.login(loginDto);

        assertEquals("token12345", response.getToken());
    }

}
