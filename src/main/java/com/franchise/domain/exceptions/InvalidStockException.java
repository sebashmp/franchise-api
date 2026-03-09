package com.franchise.domain.exceptions;

public class InvalidStockException extends DomainException {

    public InvalidStockException(String message) {
        super("INVALID_STOCK", message);
    }
}
