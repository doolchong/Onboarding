package com.example.onboarding.common.exception;

import com.example.onboarding.common.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidRequestException(InvalidRequestException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.from(
                HttpStatus.BAD_REQUEST, e.getMessage(), URI.create(request.getRequestURI())));
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ExceptionResponse> handleServerException(ServerException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionResponse.from(
                HttpStatus.NOT_FOUND, e.getMessage(), URI.create(request.getRequestURI())));
    }

    @ExceptionHandler(TokenStorageException.class)
    public ResponseEntity<ExceptionResponse> handleTokenStorageException(TokenStorageException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionResponse.from(
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), URI.create(request.getRequestURI())));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionResponse.from(
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), URI.create(request.getRequestURI())));
    }
}
