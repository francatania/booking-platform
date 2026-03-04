 package com.booking.companyservice.model.dto;

import com.booking.companyservice.entity.Company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CompanyResponse {

    private Long companyId;
    private String companyName;

    public static CompanyResponse from(Company company){
        return CompanyResponse.builder()
        .companyId(company.getId())
        .companyName(company.getName())
        .build();
}
}