package com.booking.companyservice.controller;

import com.booking.companyservice.config.JwtFilter;
import com.booking.companyservice.config.SecurityConfig;
import com.booking.companyservice.exception.CompanyNotFoundException;
import com.booking.companyservice.exception.CompanyServiceNotFoundException;
import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.model.dto.CreateCompanyServiceRequest;
import com.booking.companyservice.model.dto.UpdateCompanyServiceRequest;
import com.booking.companyservice.service.interfaces.ICompanyServiceService;
import com.booking.companyservice.service.interfaces.IJwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyServiceController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class CompanyServiceControllerTest {

    private static final String FAKE_TOKEN = "fake-jwt-token";
    private static final String AUTH_HEADER = "Bearer " + FAKE_TOKEN;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICompanyServiceService companyServiceService;

    @MockBean
    private IJwtService jwtService;

    @BeforeEach
    void setUpJwt() {
        when(jwtService.isTokenValid(FAKE_TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(FAKE_TOKEN)).thenReturn("admin@test.com");
        when(jwtService.extractRole(FAKE_TOKEN)).thenReturn("ADMIN");
        when(jwtService.extractCompanyId(FAKE_TOKEN)).thenReturn(1L);
    }

    private CreateCompanyServiceRequest buildCreateRequest() {
        CreateCompanyServiceRequest dto = new CreateCompanyServiceRequest();
        dto.setName("Haircut");
        dto.setDurationMinutes(30);
        dto.setPrice(BigDecimal.valueOf(1500));
        return dto;
    }

    @Test
    void getServicesByCompany_whenCompanyExists_returns200() throws Exception {
        CompanyServiceResponse s1 = CompanyServiceResponse.builder().id(1L).name("Haircut").build();
        CompanyServiceResponse s2 = CompanyServiceResponse.builder().id(2L).name("Other").build();

        when(companyServiceService.getServicesByCompany(1L)).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/companies/1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getServicesByCompany_whenCompanyNotFound_returns404() throws Exception {
        when(companyServiceService.getServicesByCompany(1L)).thenThrow(new CompanyNotFoundException(1L));

        mockMvc.perform(get("/companies/1/services"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createService_whenValidRequest_returns201() throws Exception {
        CompanyServiceResponse response = CompanyServiceResponse.builder().id(1L).name("Haircut").build();

        when(companyServiceService.createService(any(), eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/companies/1/services")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Haircut"));
    }

    @Test
    void editService_whenValidRequest_returns200() throws Exception {
        UpdateCompanyServiceRequest dto = new UpdateCompanyServiceRequest();
        dto.setName("Updated haircut");

        CompanyServiceResponse response = CompanyServiceResponse.builder().id(1L).name("Updated haircut").build();

        when(companyServiceService.editService(any(), eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/services/1")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated haircut"));
    }

    @Test
    void activateService_whenValidRequest_returns200() throws Exception {
        CompanyServiceResponse response = CompanyServiceResponse.builder().id(1L).isActive(true).build();

        when(companyServiceService.activateService(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/services/1/activate")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void deactivateService_whenValidRequest_returns200() throws Exception {
        CompanyServiceResponse response = CompanyServiceResponse.builder().id(1L).isActive(false).build();

        when(companyServiceService.deactivateService(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/services/1/deactivate")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void editService_whenServiceNotFound_returns404() throws Exception {
        UpdateCompanyServiceRequest dto = new UpdateCompanyServiceRequest();

        when(companyServiceService.editService(any(), eq(1L), any())).thenThrow(new CompanyServiceNotFoundException(1L));

        mockMvc.perform(patch("/services/1")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

}
