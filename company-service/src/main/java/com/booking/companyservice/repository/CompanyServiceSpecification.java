package com.booking.companyservice.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.booking.companyservice.entity.CompanyServiceEntity;

public class CompanyServiceSpecification {

    public static Specification<CompanyServiceEntity> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<CompanyServiceEntity> hasCompany(Long companyId) {
        return (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<CompanyServiceEntity> nameContains(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<CompanyServiceEntity> priceFrom(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<CompanyServiceEntity> priceTo(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}
