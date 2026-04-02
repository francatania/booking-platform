package com.booking.companyservice.exception;

import lombok.Getter;

@Getter
public class CompanyNotFoundException extends RuntimeException {

    private final Long companyId;

    public CompanyNotFoundException(Long id) {
        super(String.valueOf(id));
        this.companyId = id;
    }
}
