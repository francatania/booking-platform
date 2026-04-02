package com.booking.authservice.exception;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends RuntimeException {

    private final String email;

    public UserAlreadyExistsException(String email) {
        super(email);
        this.email = email;
    }
}
