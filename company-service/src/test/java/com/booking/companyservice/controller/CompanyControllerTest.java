package com.booking.companyservice.controller;

import com.booking.companyservice.exception.CompanyAlreadyExistsException;
import com.booking.companyservice.exception.CompanyNotFoundException;
import com.booking.companyservice.model.dto.CompanyDetailResponse;
import com.booking.companyservice.model.dto.CompanyResponse;
import com.booking.companyservice.model.dto.CreateCompanyRequest;
import com.booking.companyservice.service.interfaces.ICompanyService;
import com.booking.companyservice.service.interfaces.IJwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICompanyService companyService;

    @MockBean
    private IJwtService jwtService;

    private CreateCompanyRequest buildRequest() {
        CreateCompanyRequest dto = new CreateCompanyRequest();
        dto.setName("Test Company");
        dto.setDescription("Description");
        dto.setAddress("Street 123");
        dto.setPhone("123456789");
        return dto;
    }

    @Test
    void createCompany_whenValidRequest_returns201() throws Exception {
        CompanyDetailResponse response = CompanyDetailResponse.builder()
                .id(1L).name("Test Company").build();

        when(companyService.createCompany(any())).thenReturn(response);

        mockMvc.perform(post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Company"));
    }

    @Test
    void createCompany_whenCompanyAlreadyExists_returns409() throws Exception {
        when(companyService.createCompany(any())).thenThrow(new CompanyAlreadyExistsException("Test Company"));

        mockMvc.perform(post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void createCompany_whenMissingRequiredField_returns400() throws Exception {
        CreateCompanyRequest dto = new CreateCompanyRequest();
        dto.setName("Test Company");

        mockMvc.perform(post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCompanies_returnsListWith200() throws Exception {
        CompanyResponse c1 = CompanyResponse.builder().companyId(1L).companyName("Company A").build();
        CompanyResponse c2 = CompanyResponse.builder().companyId(2L).companyName("Company B").build();

        when(companyService.getCompanyList()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getCompany_whenExists_returns200() throws Exception {
        CompanyDetailResponse response = CompanyDetailResponse.builder()
                .id(1L).name("Test Company").build();

        when(companyService.getCompany(1L)).thenReturn(response);

        mockMvc.perform(get("/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCompany_whenNotFound_returns404() throws Exception {
        when(companyService.getCompany(1L)).thenThrow(new CompanyNotFoundException(1L));

        mockMvc.perform(get("/companies/1"))
                .andExpect(status().isNotFound());
    }

}
