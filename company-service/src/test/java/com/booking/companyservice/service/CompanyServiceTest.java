package com.booking.companyservice.service;

import com.booking.companyservice.entity.Company;
import com.booking.companyservice.exception.CompanyAlreadyExistsException;
import com.booking.companyservice.exception.CompanyNotFoundException;
import com.booking.companyservice.model.dto.CompanyDetailResponse;
import com.booking.companyservice.model.dto.CompanyResponse;
import com.booking.companyservice.model.dto.CreateCompanyRequest;
import com.booking.companyservice.repository.CompanyRepository;
import com.booking.companyservice.service.impl.CompanyService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    private static final Long COMPANY_ID = 1L;
    private static final String COMPANY_NAME = "mock company";
    private static final String COMPANY_DESCRIPTION = "mock description";
    private static final String COMPANY_ADDRESS = "mock address";
    private static final String COMPANY_PHONE = "mock phone";

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private CreateCompanyRequest buildCompanyRequest(){
        CreateCompanyRequest dto = new CreateCompanyRequest();
        dto.setName(COMPANY_NAME);
        dto.setDescription(COMPANY_DESCRIPTION);
        dto.setAddress(COMPANY_ADDRESS);
        dto.setPhone(COMPANY_PHONE);

        return dto;
    }

    @Test
    void createCompany_whenCompanyAlreadyExists_throwsException(){
        CreateCompanyRequest dto = this.buildCompanyRequest();
        when(companyRepository.existsByName(dto.getName()))
            .thenReturn(true);
        
        assertThrows(CompanyAlreadyExistsException.class,()-> companyService.createCompany(dto));
    }

    @Test
    void createCompany_whenCompanyIsNew_returnsCompanyCreated(){
        CreateCompanyRequest dto = this.buildCompanyRequest();
        Company company  = Company.builder()
                                            .id(COMPANY_ID)
                                            .name(COMPANY_NAME)
                                            .description(COMPANY_DESCRIPTION)
                                            .phone(COMPANY_PHONE)
                                            .address(COMPANY_ADDRESS)
                                            .build();

        when(companyRepository.existsByName(dto.getName()))
            .thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        CompanyDetailResponse response = companyService.createCompany(dto);

        assertEquals(company.getName(), response.getName());
        assertEquals(company.getId(), response.getId());
        assertEquals(company.getDescription(), response.getDescription());
        assertEquals(company.getPhone(), response.getPhone());
        assertEquals(company.getAddress(), response.getAddress());
    }

    @Test 
    void getCompanyList_whenIsNotEmpty_returnsList(){
        Company company1 = Company.builder()
            .id(1l)
            .name("company")
            .build();
        Company company2 = Company.builder()
            .id(2l)
            .name("comp2")
            .build();
        List<Company> list = List.of(company1, company2);
        
        when(companyRepository.findAll()).thenReturn(list);

        List<CompanyResponse> result = companyService.getCompanyList();

        assertEquals(2, result.size());
    }

    @Test
    void getCompany_whenDoesnotExists_throwsException(){
        when(companyRepository.findById(any(Long.class)))
            .thenReturn(Optional.empty());
        
        assertThrows(CompanyNotFoundException.class, ()-> companyService.getCompany(COMPANY_ID));
    }

    @Test
    void getCompanyEntity_whenDoesNotExist_throwsException(){
        when(companyRepository.findById(any(Long.class)))
            .thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class, () -> companyService.getCompanyEntity(COMPANY_ID));
    }

    @Test
    void getCompanyEntity_whenExists_returnsEntity(){
        Company company = Company.builder()
            .id(COMPANY_ID)
            .name(COMPANY_NAME)
            .build();
        when(companyRepository.findById(COMPANY_ID))
            .thenReturn(Optional.of(company));

        Company result = companyService.getCompanyEntity(COMPANY_ID);

        assertEquals(COMPANY_ID, result.getId());
        assertEquals(COMPANY_NAME, result.getName());
    }

    @Test
    void getCompany_whenCompanyExists_returnCompany(){
        Company company = Company.builder()
            .id(COMPANY_ID)
            .name(COMPANY_NAME)
            .description(COMPANY_DESCRIPTION)
            .address(COMPANY_ADDRESS)
            .phone(COMPANY_PHONE)
            .build();
        when(companyRepository.findById(any(Long.class)))
            .thenReturn(Optional.of(company));
        
        CompanyDetailResponse result = companyService.getCompany(COMPANY_ID);

        assertEquals(COMPANY_ID, result.getId());
        assertEquals(COMPANY_NAME, result.getName());
        assertEquals(COMPANY_DESCRIPTION, result.getDescription());
        assertEquals(COMPANY_ADDRESS, result.getAddress());
        assertEquals(COMPANY_PHONE, result.getPhone());
    }

}
