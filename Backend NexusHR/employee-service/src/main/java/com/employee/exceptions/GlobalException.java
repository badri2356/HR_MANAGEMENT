package com.employee.exceptions;


import com.employee.dtos.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> exceptionHandler(UserAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<?> exceptionHandler(AccountInactiveException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> exceptionHandler(ResourceNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicate(DuplicateResourceException exception) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(status.value(), exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {

        String action = switch (request.getMethod()) {
            case "POST" -> "create";
            case "PUT", "PATCH" -> "update";
            case "DELETE" -> "delete";
            default -> "view";
        };

        String message = "You are not allowed to " + action + " this resource";

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), message, Instant.now()));
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<?> handleInvalidFileException(InvalidFileException ex) {
        int status = HttpStatus.BAD_REQUEST.value();

        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(status, ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        int status = HttpStatus.BAD_REQUEST.value();

        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(status, ex.getMessage(), Instant.now()));
    }
}
