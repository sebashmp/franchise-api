package com.franchise.domain.exceptions;

public class DatabaseUnavailableException extends DomainException {

    public DatabaseUnavailableException(String message) {
        super("DATABASE_UNAVAILABLE", message);
    }
}
