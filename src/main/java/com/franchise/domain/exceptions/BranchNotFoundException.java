package com.franchise.domain.exceptions;

public class BranchNotFoundException extends DomainException {

    public BranchNotFoundException(String branchId) {
        super("BRANCH_NOT_FOUND",
                "Branch with id '" + branchId + "' was not found.");
    }
}
