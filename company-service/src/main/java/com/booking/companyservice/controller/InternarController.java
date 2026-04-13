package com.booking.companyservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booking.companyservice.model.dto.CompanyServiceResponse;
import com.booking.companyservice.service.interfaces.ICompanyServiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Internal endpoints for service-to-service service lookups.
 * Not exposed through the API gateway; consumed by booking-service.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
@Tag(name = "Internal - Services", description = "Service-to-service service lookups")
public class InternarController {
    private final ICompanyServiceService companyService;

    @Operation(summary = "Get service by ID")
    @GetMapping("/services/{serviceId}")
    public ResponseEntity<CompanyServiceResponse> getService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(companyService.getServiceById(serviceId));
    }

    @Operation(summary = "Get services by list of IDs")
    @GetMapping("/services")
    public ResponseEntity<List<CompanyServiceResponse>> getServicesByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(companyService.getServicesByIds(ids));
    }
}
