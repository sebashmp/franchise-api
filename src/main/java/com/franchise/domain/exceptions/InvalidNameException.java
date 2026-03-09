package com.franchise.domain.exceptions;

public class InvalidNameException extends DomainException {

    public InvalidNameException(String message) {
        super("INVALID_NAME", message);
    }
}
