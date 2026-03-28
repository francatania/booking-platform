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

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternarController {
    private final ICompanyServiceService companyService;

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<CompanyServiceResponse> getService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(companyService.getServiceById(serviceId));
    }

    @GetMapping("/services")
    public ResponseEntity<List<CompanyServiceResponse>> getServicesByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(companyService.getServicesByIds(ids));
    }
}
