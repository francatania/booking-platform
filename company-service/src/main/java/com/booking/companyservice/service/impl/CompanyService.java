package com.booking.companyservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.booking.companyservice.entity.Company;
import com.booking.companyservice.exception.CompanyAlreadyExistsException;
import com.booking.companyservice.exception.CompanyNotFoundException;
import com.booking.companyservice.model.dto.CompanyDetailResponse;
import com.booking.companyservice.model.dto.CompanyResponse;
import com.booking.companyservice.model.dto.CreateCompanyRequest;
import com.booking.companyservice.repository.CompanyRepository;
import com.booking.companyservice.service.interfaces.ICompanyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService implements ICompanyService {
    private final CompanyRepository repository;

    public CompanyDetailResponse createCompany(CreateCompanyRequest dto){
        if(repository.existsByName(dto.getName())){
            throw new CompanyAlreadyExistsException(dto.getName());
        }

        Company company = Company.builder()
                            .name(dto.getName())
                            .phone(dto.getPhone())
                            .address(dto.getAddress())
                            .description(dto.getDescription())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

        return CompanyDetailResponse.from(repository.save(company));
    }

    public List<CompanyResponse> getCompanyList(){
        return repository.findAll().stream()
                        .map(CompanyResponse::from)
                        .toList();
    }

    public CompanyDetailResponse getCompany(Long id){
        Optional<Company> company = repository.findById(id);
        if(company.isEmpty()){
            throw new CompanyNotFoundException(id);
        }

        return CompanyDetailResponse.from(company.get());
    }

    public Company getCompanyEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
    }
}
