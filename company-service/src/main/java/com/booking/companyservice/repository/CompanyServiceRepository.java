package com.booking.companyservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.booking.companyservice.entity.CompanyServiceEntity;

@Repository
public interface CompanyServiceRepository extends JpaRepository<CompanyServiceEntity, Long>{

    List<CompanyServiceEntity> findByCompanyId(Long companyId);
}
