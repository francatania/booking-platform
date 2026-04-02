package com.booking.companyservice.exception;

import lombok.Getter;

@Getter
public class CompanyServiceNotFoundException extends RuntimeException {

    private final Long serviceId;

    public CompanyServiceNotFoundException(Long id) {
        super(String.valueOf(id));
        this.serviceId = id;
    }
}
