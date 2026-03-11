package com.booking.companyservice.service.interfaces;

import java.util.List;

import com.booking.companyservice.entity.Company;
import com.booking.companyservice.model.dto.CompanyDetailResponse;
import com.booking.companyservice.model.dto.CompanyResponse;
import com.booking.companyservice.model.dto.CreateCompanyRequest;

public interface ICompanyService {

    CompanyDetailResponse createCompany(CreateCompanyRequest dto);

    List<CompanyResponse> getCompanyList();

    CompanyDetailResponse getCompany(Long id);

    Company getCompanyEntity(Long id);
}
