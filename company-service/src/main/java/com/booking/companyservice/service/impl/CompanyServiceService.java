package com.booking.companyservice.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.booking.companyservice.config.UserPrincipal;
import com.booking.companyservice.entity.Company;
import com.booking.companyservice.entity.CompanyServiceEntity;
import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.model.dto.CreateCompanyServiceRequest;
import com.booking.companyservice.model.dto.UpdateCompanyServiceRequest;
import com.booking.companyservice.exception.CompanyServiceNotFoundException;
import com.booking.companyservice.repository.CompanyServiceRepository;
import com.booking.companyservice.repository.CompanyServiceSpecification;
import com.booking.companyservice.service.interfaces.ICompanyService;
import com.booking.companyservice.service.interfaces.ICompanyServiceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceService implements ICompanyServiceService {
    private final ICompanyService companyService;
    private final CompanyServiceRepository repository;

    public List<CompanyServiceResponse> getServicesByCompany(Long companyId) {
        companyService.getCompanyEntity(companyId);
        return repository.findByCompanyId(companyId).stream()
                .map(CompanyServiceResponse::from)
                .toList();
    }

    public CompanyServiceResponse createService(CreateCompanyServiceRequest dto, Long companyId, UserPrincipal principal){
        checkOwnership(principal, companyId);

        Company company = companyService.getCompanyEntity(companyId);

        CompanyServiceEntity service = CompanyServiceEntity.builder()
                                    .company(company)
                                    .description(dto.getDescription())
                                    .durationMinutes(dto.getDurationMinutes())
                                    .isActive(true)
                                    .name(dto.getName())
                                    .price(dto.getPrice())
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
        return CompanyServiceResponse.from(repository.save(service));
    }

    public CompanyServiceResponse editService(UpdateCompanyServiceRequest dto, Long serviceId, UserPrincipal principal){
        CompanyServiceEntity service = this.getServiceEntity(serviceId);
        checkOwnership(principal, service.getCompany().getId());

        if(dto.getName() != null){
            service.setName(dto.getName());
        }
        if(dto.getDescription() != null){
            service.setDescription(dto.getDescription());
        }
        if(dto.getDurationMinutes() != null){
            service.setDurationMinutes(dto.getDurationMinutes());
        }
        if(dto.getPrice() != null){
            service.setPrice(dto.getPrice());
        }

        return CompanyServiceResponse.from(repository.save(service));
    }

    private CompanyServiceEntity getServiceEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CompanyServiceNotFoundException(id));
    }

    public List<CompanyServiceResponse> getServicesByIds(List<Long> ids) {
        return repository.findAllByIdIn(ids).stream()
                .map(CompanyServiceResponse::from)
                .toList();
    }

    public CompanyServiceResponse getServiceById(Long id) {
        CompanyServiceEntity service = getServiceEntity(id);
        if (!service.getIsActive()) {
            throw new IllegalStateException("Service is not active");
        }
        return CompanyServiceResponse.from(service);
    }

    public CompanyServiceResponse activateService(Long id, UserPrincipal principal) {
        CompanyServiceEntity service = getServiceEntity(id);
        checkOwnership(principal, service.getCompany().getId());
        service.setIsActive(true);
        return CompanyServiceResponse.from(repository.save(service));
    }

    public CompanyServiceResponse deactivateService(Long id, UserPrincipal principal) {
        CompanyServiceEntity service = getServiceEntity(id);
        checkOwnership(principal, service.getCompany().getId());
        service.setIsActive(false);
        return CompanyServiceResponse.from(repository.save(service));
    }

    private void checkOwnership(UserPrincipal principal, Long companyId) {
        if ("SUPER_ADMIN".equals(principal.role())) {
            return;
        }
        if (!"ADMIN".equals(principal.role()) && !"OPERATOR".equals(principal.role())) {
        throw new AccessDeniedException("Only admins can manage services");
    }
        if (!companyId.equals(principal.companyId())) {
            throw new AccessDeniedException("You don't have permission to manage this company");
        }
    }

    @Override
    public Page<CompanyServiceResponse> getAllServices(Pageable pageable, Long companyId, String name, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<CompanyServiceEntity> spec = CompanyServiceSpecification.isActive();
        if (companyId != null) spec = spec.and(CompanyServiceSpecification.hasCompany(companyId));
        if (name != null && !name.isBlank()) spec = spec.and(CompanyServiceSpecification.nameContains(name));
        if (minPrice != null) spec = spec.and(CompanyServiceSpecification.priceFrom(minPrice));
        if (maxPrice != null) spec = spec.and(CompanyServiceSpecification.priceTo(maxPrice));
        return repository.findAll(spec, pageable).map(CompanyServiceResponse::from);
    }
}
