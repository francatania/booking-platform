package com.booking.companyservice.service.interfaces;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.booking.companyservice.config.UserPrincipal;
import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.model.dto.CreateCompanyServiceRequest;
import com.booking.companyservice.model.dto.UpdateCompanyServiceRequest;

/**
 * Service contract for managing a company's service catalog.
 */
public interface ICompanyServiceService {

    /**
     * Returns all services belonging to a specific company.
     *
     * @param companyId the company ID
     * @return list of {@link CompanyServiceResponse}
     */
    List<CompanyServiceResponse> getServicesByCompany(Long companyId);

    /**
     * Creates a new service under the given company.
     * Only the company's own ADMIN can perform this action.
     *
     * @param dto       the service data (name, description, duration, price)
     * @param companyId the target company ID
     * @param principal the authenticated user performing the request
     * @return the created {@link CompanyServiceResponse}
     * @throws com.booking.companyservice.exception.CompanyNotFoundException if the company does not exist
     */
    CompanyServiceResponse createService(CreateCompanyServiceRequest dto, Long companyId, UserPrincipal principal);

    /**
     * Updates an existing service's fields.
     * Only the company's own ADMIN can perform this action.
     *
     * @param dto       the fields to update (partial update)
     * @param serviceId the service ID to update
     * @param principal the authenticated user performing the request
     * @return the updated {@link CompanyServiceResponse}
     * @throws com.booking.companyservice.exception.CompanyServiceNotFoundException if the service does not exist
     */
    CompanyServiceResponse editService(UpdateCompanyServiceRequest dto, Long serviceId, UserPrincipal principal);

    /**
     * Activates a service, making it available for booking.
     *
     * @param id        the service ID
     * @param principal the authenticated user performing the request
     * @return the updated {@link CompanyServiceResponse}
     * @throws com.booking.companyservice.exception.CompanyServiceNotFoundException if the service does not exist
     */
    CompanyServiceResponse activateService(Long id, UserPrincipal principal);

    /**
     * Deactivates a service, preventing it from being booked.
     *
     * @param id        the service ID
     * @param principal the authenticated user performing the request
     * @return the updated {@link CompanyServiceResponse}
     * @throws com.booking.companyservice.exception.CompanyServiceNotFoundException if the service does not exist
     */
    CompanyServiceResponse deactivateService(Long id, UserPrincipal principal);

    /**
     * Returns a single service by its ID.
     * Used internally by booking-service to validate that a service exists and is active.
     *
     * @param id the service ID
     * @return the {@link CompanyServiceResponse}
     * @throws com.booking.companyservice.exception.CompanyServiceNotFoundException if the service does not exist
     */
    CompanyServiceResponse getServiceById(Long id);

    /**
     * Returns a list of services matching the given IDs.
     * Used internally by booking-service for batch enrichment (service names).
     *
     * @param ids list of service IDs to fetch
     * @return list of matching {@link CompanyServiceResponse}
     */
    List<CompanyServiceResponse> getServicesByIds(List<Long> ids);

    /**
     * Returns a paginated and optionally filtered list of all services across all companies.
     *
     * @param pageable   pagination and sorting parameters
     * @param companyId  optional filter by company
     * @param name       optional filter by service name (partial match)
     * @param minPrice   optional minimum price filter
     * @param maxPrice   optional maximum price filter
     * @return paginated {@link CompanyServiceResponse} results
     */
    Page<CompanyServiceResponse> getAllServices(Pageable pageable, Long companyId, String name, BigDecimal minPrice, BigDecimal maxPrice);
}
