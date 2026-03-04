package com.booking.companyservice.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.booking.companyservice.entity.CompanyServiceEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CompanyServiceResponse {

    private Long id;
    private Long companyId;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static CompanyServiceResponse from(CompanyServiceEntity service) {
        return CompanyServiceResponse.builder()
                .id(service.getId())
                .companyId(service.getCompany().getId())
                .name(service.getName())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .isActive(service.getIsActive())
                .createdAt(service.getCreatedAt())
                .build();
    }
}
