package com.booking.companyservice.service.interfaces;

import java.util.List;

import com.booking.companyservice.entity.Company;
import com.booking.companyservice.model.dto.CompanyDetailResponse;
import com.booking.companyservice.model.dto.CompanyResponse;
import com.booking.companyservice.model.dto.CreateCompanyRequest;

/**
 * Service contract for company management.
 */
public interface ICompanyService {

    /**
     * Creates a new company.
     *
     * @param dto the company data (name, description, address, phone)
     * @return a {@link CompanyDetailResponse} with the created company's full data
     * @throws com.booking.companyservice.exception.CompanyAlreadyExistsException if a company with the same name already exists
     */
    CompanyDetailResponse createCompany(CreateCompanyRequest dto);

    /**
     * Returns a summary list of all registered companies.
     *
     * @return list of {@link CompanyResponse} with basic company info
     */
    List<CompanyResponse> getCompanyList();

    /**
     * Returns the full details of a company by its ID.
     *
     * @param id the company ID
     * @return a {@link CompanyDetailResponse} with the company's full data including services
     * @throws com.booking.companyservice.exception.CompanyNotFoundException if no company exists with the given ID
     */
    CompanyDetailResponse getCompany(Long id);

    /**
     * Returns the raw {@link Company} entity by its ID.
     * Intended for internal use within the service layer.
     *
     * @param id the company ID
     * @return the {@link Company} entity
     * @throws com.booking.companyservice.exception.CompanyNotFoundException if no company exists with the given ID
     */
    Company getCompanyEntity(Long id);
}
