package com.booking.companyservice.exception;

public class CompanyAlreadyExistsException extends RuntimeException{
    public CompanyAlreadyExistsException(String name){
        super("Company already exists with name: " + name);
    }
}
