package com.booking.authservice.controller;

import com.booking.authservice.exception.InvalidCredentialsException;
import com.booking.authservice.exception.UserAlreadyExistsException;
import com.booking.authservice.service.impl.MessageService;
import com.booking.authservice.model.dto.AuthResponse;
import com.booking.authservice.model.dto.LoginRequest;
import com.booking.authservice.model.dto.RegisterRequest;
import com.booking.authservice.model.dto.UserResponse;
import com.booking.authservice.model.enums.UserRole;
import com.booking.authservice.service.interfaces.IJwtService;
import com.booking.authservice.service.interfaces.IUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) 
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    @MockBean
    private IJwtService jwtService;

    @MockBean
    private MessageService messageService;

    @Test
    void register_whenValidRequest_returns200() throws Exception {
        RegisterRequest body = new RegisterRequest();
        body.setEmail("john@mail.com");
        body.setUsername("john");
        body.setPassword("pass123");

        UserResponse mockResponse = new UserResponse();
        mockResponse.setEmail("john@mail.com");
        mockResponse.setRole(UserRole.USER);

        when(userService.register(any(RegisterRequest.class), eq(false))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_whenEmailAlreadyExists_returns409() throws Exception {
        RegisterRequest body = new RegisterRequest();
        body.setEmail("john@mail.com");
        body.setUsername("john");
        body.setPassword("pass123");

        when(userService.register(any(RegisterRequest.class), eq(false)))
                .thenThrow(new UserAlreadyExistsException("john@mail.com"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_admin_whenValidRequest_returns200() throws JsonProcessingException, Exception{
        RegisterRequest body = new RegisterRequest();
        body.setEmail("john@mail.com");
        body.setUsername("john");
        body.setPassword("pass123");

        UserResponse mockResponse = new UserResponse();
        mockResponse.setEmail("john@mail.com");
        mockResponse.setRole(UserRole.ADMIN);

        when(userService.register(any(RegisterRequest.class), eq(true))).thenReturn(mockResponse);
        
        mockMvc.perform(post("/auth/register-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_whenValidCredentials_returns200WithToken() throws Exception {
        LoginRequest body = new LoginRequest();
        body.setUsername("john");
        body.setPassword("pass123");

        when(userService.login(any(LoginRequest.class)))
                .thenReturn(AuthResponse.builder().token("jwt-token").build());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_whenInvalidCredentials_returns401() throws Exception {
        LoginRequest body = new LoginRequest();
        body.setUsername("john");
        body.setPassword("wrong");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
