package com.franchise.domain.api;

import com.franchise.domain.model.Branch;
import reactor.core.publisher.Mono;

public interface IBranchServicePort {

    Mono<Branch> addBranch(String franchiseId, String name);

    Mono<Branch> updateBranchName(String branchId, String newName);
}
