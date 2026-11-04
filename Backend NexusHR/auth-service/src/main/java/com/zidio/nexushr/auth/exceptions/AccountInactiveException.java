package com.zidio.nexushr.auth.exceptions;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException(String message) {
        super(message);
    }
}
