package com.employee.exceptions;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException(String message) {
        super(message);
    }
}
