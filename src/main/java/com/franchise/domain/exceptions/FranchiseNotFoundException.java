package com.franchise.domain.exceptions;

public class FranchiseNotFoundException extends DomainException {

    public FranchiseNotFoundException(String franchiseId) {
        super("FRANCHISE_NOT_FOUND",
                "Franchise with id '" + franchiseId + "' was not found.");
    }
}
