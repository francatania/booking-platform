package com.booking.companyservice.service.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.booking.companyservice.config.UserPrincipal;
import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.model.dto.CreateCompanyServiceRequest;
import com.booking.companyservice.model.dto.UpdateCompanyServiceRequest;

public interface ICompanyServiceService {

    List<CompanyServiceResponse> getServicesByCompany(Long companyId);

    CompanyServiceResponse createService(CreateCompanyServiceRequest dto, Long companyId, UserPrincipal principal);

    CompanyServiceResponse editService(UpdateCompanyServiceRequest dto, Long serviceId, UserPrincipal principal);

    CompanyServiceResponse activateService(Long id, UserPrincipal principal);

    CompanyServiceResponse deactivateService(Long id, UserPrincipal principal);

    CompanyServiceResponse getServiceById(Long id);
    Page<CompanyServiceResponse> getAllServices(Pageable pageable);
}
