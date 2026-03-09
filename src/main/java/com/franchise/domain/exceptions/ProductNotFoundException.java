package com.franchise.domain.exceptions;

public class ProductNotFoundException extends DomainException {

    public ProductNotFoundException(String productId) {
        super("PRODUCT_NOT_FOUND",
                "Product with id '" + productId + "' was not found.");
    }
}
