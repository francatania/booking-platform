package com.booking.companyservice.exception;

import lombok.Getter;

@Getter
public class CompanyAlreadyExistsException extends RuntimeException {

    private final String name;

    public CompanyAlreadyExistsException(String name) {
        super(name);
        this.name = name;
    }
}
