package com.booking.companyservice.exception;

public class CompanyServiceNotFoundException extends RuntimeException {

    public CompanyServiceNotFoundException(Long id) {
        super("Service not found with id: " + id);
    }
}
