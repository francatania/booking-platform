package com.booking.companyservice.model.dto;

import java.time.LocalDateTime;

import com.booking.companyservice.entity.Company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CompanyDetailResponse {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private LocalDateTime createdAt;

    public static CompanyDetailResponse from(Company company) {
        return CompanyDetailResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .address(company.getAddress())
                .phone(company.getPhone())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
