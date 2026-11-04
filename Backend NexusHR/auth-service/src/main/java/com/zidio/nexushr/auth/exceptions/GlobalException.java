package com.zidio.nexushr.auth.exceptions;

import com.zidio.nexushr.auth.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> exceptionHandler(UserAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, exception.getMessage(), java.time.Instant.now()));
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<?> exceptionHandler(AccountInactiveException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, exception.getMessage(), java.time.Instant.now()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> exceptionHandler(ResourceNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, exception.getMessage(), java.time.Instant.now()));
    }

}
