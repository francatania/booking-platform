package com.booking.companyservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booking.companyservice.model.dto.CompanyDetailResponse;
import com.booking.companyservice.model.dto.CompanyResponse;
import com.booking.companyservice.model.dto.CreateCompanyRequest;
import com.booking.companyservice.service.interfaces.ICompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company management")
public class CompanyController {
    private final ICompanyService service;

    @Operation(summary = "Create a new company (requires SUPER_ADMIN)")
    @PostMapping
    public ResponseEntity<CompanyDetailResponse> createCompany(@RequestBody @Valid CreateCompanyRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCompany(dto));
    }

    @Operation(summary = "List all companies")
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getCompanies() {
        return ResponseEntity.ok(service.getCompanyList());
    }

    @Operation(summary = "Get company by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDetailResponse> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCompany(id));
    }
}
