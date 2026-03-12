package com.booking.authservice.controller;

import com.booking.authservice.model.dto.LoginRequest;
import com.booking.authservice.service.interfaces.IJwtService;
import com.booking.authservice.service.interfaces.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    @MockBean
    private IJwtService jwtService;

    @Test
    void whenIllegalArgumentException_returns400() throws Exception {
        LoginRequest body = new LoginRequest();
        body.setUsername("john");
        body.setPassword("pass123");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("invalid argument"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid argument"));
    }

    @Test
    void whenRuntimeException_returns500() throws Exception {
        LoginRequest body = new LoginRequest();
        body.setUsername("john");
        body.setPassword("pass123");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("unexpected error"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("unexpected error"));
    }
}
