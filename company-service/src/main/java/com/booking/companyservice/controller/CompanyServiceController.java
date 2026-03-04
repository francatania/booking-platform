package com.booking.companyservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.model.dto.CreateCompanyServiceRequest;
import com.booking.companyservice.model.dto.UpdateCompanyServiceRequest;
import com.booking.companyservice.service.CompanyServiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CompanyServiceController {

    private final CompanyServiceService service;

    @GetMapping("/companies/{companyId}/services")
    public ResponseEntity<List<CompanyServiceResponse>> getServicesByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(service.getServicesByCompany(companyId));
    }

    @PostMapping("/companies/{companyId}/services")
    public ResponseEntity<CompanyServiceResponse> createService(
            @PathVariable Long companyId,
            @RequestBody @Valid CreateCompanyServiceRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createService(dto, companyId));
    }

    @PatchMapping("/services/{id}")
    public ResponseEntity<CompanyServiceResponse> editService(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCompanyServiceRequest dto) {
        return ResponseEntity.ok(service.editService(dto, id));
    }

    @PatchMapping("/services/{id}/activate")
    public ResponseEntity<CompanyServiceResponse> activateService(@PathVariable Long id) {
        return ResponseEntity.ok(service.activateService(id));
    }

    @PatchMapping("/services/{id}/deactivate")
    public ResponseEntity<CompanyServiceResponse> deactivateService(@PathVariable Long id) {
        return ResponseEntity.ok(service.deactivateService(id));
    }

}
