package com.booking.authservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.booking.authservice.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String email);

    List<User> findAllByIdIn(List<Long> ids);

    List<User> findAllByCompanyIdAndRole(Long companyId, com.booking.authservice.model.enums.UserRole role);
}
