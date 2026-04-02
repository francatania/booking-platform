package com.booking.companyservice.controller;

import com.booking.companyservice.exception.CompanyAlreadyExistsException;
import com.booking.companyservice.exception.CompanyNotFoundException;
import com.booking.companyservice.exception.CompanyServiceNotFoundException;
import com.booking.companyservice.model.dto.ErrorResponse;
import com.booking.companyservice.service.impl.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageService messageService;

    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    private Locale locale(HttpServletRequest request) {
        return MessageService.resolveLocale(request.getHeader("Accept-Language"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleInactiveService(IllegalStateException ex, HttpServletRequest request) {
        String msg = messageService.getMessage("service.not.active", locale(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), msg));
    }

    @ExceptionHandler(CompanyAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCompanyAlreadyExists(CompanyAlreadyExistsException ex, HttpServletRequest request) {
        String msg = messageService.getMessage("company.already.exists", locale(request), ex.getName());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), msg));
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCompanyNotFound(CompanyNotFoundException ex, HttpServletRequest request) {
        String msg = messageService.getMessage("company.not.found", locale(request), ex.getCompanyId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), msg));
    }

    @ExceptionHandler(CompanyServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleServiceNotFound(CompanyServiceNotFoundException ex, HttpServletRequest request) {
        String msg = messageService.getMessage("service.not.found", locale(request), ex.getServiceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), msg));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String msg = messageService.getMessage("only.admins", locale(request));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }
}
