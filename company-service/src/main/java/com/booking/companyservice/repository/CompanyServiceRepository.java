package com.booking.companyservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.booking.companyservice.entity.CompanyServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CompanyServiceRepository extends JpaRepository<CompanyServiceEntity, Long>{

    List<CompanyServiceEntity> findByCompanyId(Long companyId);

    Page<CompanyServiceEntity> findByIsActiveTrue(Pageable pageable);

    List<CompanyServiceEntity> findAllByIdIn(List<Long> ids);
}
