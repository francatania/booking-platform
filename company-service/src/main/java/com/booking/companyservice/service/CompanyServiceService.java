package com.booking.companyservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.booking.companyservice.entity.Company;
import com.booking.companyservice.entity.CompanyServiceEntity;
import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.model.dto.CreateCompanyServiceRequest;
import com.booking.companyservice.model.dto.UpdateCompanyServiceRequest;
import com.booking.companyservice.exception.CompanyServiceNotFoundException;
import com.booking.companyservice.repository.CompanyServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceService {
    private final CompanyService companyService;
    private final CompanyServiceRepository repository;

    public List<CompanyServiceResponse> getServicesByCompany(Long companyId) {
        companyService.getCompanyEntity(companyId);
        return repository.findByCompanyId(companyId).stream()
                .map(CompanyServiceResponse::from)
                .toList();
    }

    public CompanyServiceResponse createService(CreateCompanyServiceRequest dto, Long companyId){
        
        Company company = companyService.getCompanyEntity(companyId);


        CompanyServiceEntity service = CompanyServiceEntity.builder()
                                    .company(company)
                                    .description(dto.getDescription())
                                    .durationMinutes(dto.getDurationMinutes())
                                    .isActive(true)
                                    .name(dto.getName())
                                    .price(dto.getPrice())
                                    .build();
        return CompanyServiceResponse.from(repository.save(service));
    }

    public CompanyServiceResponse editService(UpdateCompanyServiceRequest dto, Long serviceId){
        CompanyServiceEntity service = this.getServiceEntity(serviceId);
        
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

    public CompanyServiceResponse activateService(Long id) {
        CompanyServiceEntity service = getServiceEntity(id);
        service.setIsActive(true);
        return CompanyServiceResponse.from(repository.save(service));
    }

    public CompanyServiceResponse deactivateService(Long id) {
        CompanyServiceEntity service = getServiceEntity(id);
        service.setIsActive(false);
        return CompanyServiceResponse.from(repository.save(service));
    }
}
