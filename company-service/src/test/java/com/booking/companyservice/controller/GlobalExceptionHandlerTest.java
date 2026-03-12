package com.booking.companyservice.controller;

import com.booking.companyservice.exception.CompanyAlreadyExistsException;
import com.booking.companyservice.exception.CompanyNotFoundException;
import com.booking.companyservice.exception.CompanyServiceNotFoundException;
import com.booking.companyservice.service.interfaces.IJwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PingController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IJwtService jwtService;

    @MockBean
    private PingController pingController;

    @Test
    void handleCompanyAlreadyExists_returns409() throws Exception {
        when(pingController.ping()).thenThrow(new CompanyAlreadyExistsException("Test"));

        mockMvc.perform(get("/ping"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void handleCompanyNotFound_returns404() throws Exception {
        when(pingController.ping()).thenThrow(new CompanyNotFoundException(1L));

        mockMvc.perform(get("/ping"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void handleCompanyServiceNotFound_returns404() throws Exception {
        when(pingController.ping()).thenThrow(new CompanyServiceNotFoundException(1L));

        mockMvc.perform(get("/ping"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void handleAccessDenied_returns403() throws Exception {
        when(pingController.ping()).thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get("/ping"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void handleIllegalArgument_returns400() throws Exception {
        when(pingController.ping()).thenThrow(new IllegalArgumentException("Bad argument"));

        mockMvc.perform(get("/ping"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void handleIllegalState_returns400() throws Exception {
        when(pingController.ping()).thenThrow(new IllegalStateException("Bad state"));

        mockMvc.perform(get("/ping"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void handleRuntimeException_returns500() throws Exception {
        when(pingController.ping()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/ping"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

}
