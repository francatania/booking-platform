package com.booking.authservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booking.authservice.model.dto.InternalUserResponse;
import com.booking.authservice.model.enums.UserRole;
import com.booking.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<InternalUserResponse>> getByIds(@RequestParam List<Long> ids) {
        List<InternalUserResponse> users = userRepository.findAllByIdIn(ids)
                .stream()
                .map(InternalUserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<InternalUserResponse> getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(InternalUserResponse.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/by-company")
    public ResponseEntity<List<InternalUserResponse>> getByCompanyAndRole(
            @RequestParam Long companyId,
            @RequestParam UserRole role) {
        List<InternalUserResponse> users = userRepository.findAllByCompanyIdAndRole(companyId, role)
                .stream()
                .map(InternalUserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }
}
