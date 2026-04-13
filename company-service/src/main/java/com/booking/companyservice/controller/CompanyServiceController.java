package com.booking.companyservice.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booking.companyservice.config.UserPrincipal;
import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.model.dto.CreateCompanyServiceRequest;
import com.booking.companyservice.model.dto.UpdateCompanyServiceRequest;
import com.booking.companyservice.service.interfaces.ICompanyServiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Manages the service catalog for each company.
 * Supports filtering, pagination, and CRUD operations restricted to company ADMINs.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Services", description = "Company service catalog management")
public class CompanyServiceController {

    private final ICompanyServiceService service;

    @Operation(summary = "List all services (paginated, filterable)")
    @GetMapping("/services")
    public ResponseEntity<Page<CompanyServiceResponse>> getAllServices(
            @PageableDefault(size = 10, sort = "name") Pageable pageable,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(service.getAllServices(pageable, companyId, name, minPrice, maxPrice));
    }

    @Operation(summary = "Get all services of a company")
    @GetMapping("/companies/{companyId}/services")
    public ResponseEntity<List<CompanyServiceResponse>> getServicesByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(service.getServicesByCompany(companyId));
    }

    @Operation(summary = "Create a service for a company (operator/admin)")
    @PostMapping("/companies/{companyId}/services")
    public ResponseEntity<CompanyServiceResponse> createService(
            @PathVariable Long companyId,
            @RequestBody @Valid CreateCompanyServiceRequest dto,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createService(dto, companyId, principal));
    }

    @Operation(summary = "Update a service (operator/admin)")
    @PatchMapping("/services/{id}")
    public ResponseEntity<CompanyServiceResponse> editService(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCompanyServiceRequest dto,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(service.editService(dto, id, principal));
    }

    @Operation(summary = "Activate a service")
    @PatchMapping("/services/{id}/activate")
    public ResponseEntity<CompanyServiceResponse> activateService(
            @PathVariable Long id,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(service.activateService(id, principal));
    }

    @Operation(summary = "Deactivate a service")
    @PatchMapping("/services/{id}/deactivate")
    public ResponseEntity<CompanyServiceResponse> deactivateService(
            @PathVariable Long id,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(service.deactivateService(id, principal));
    }

}
