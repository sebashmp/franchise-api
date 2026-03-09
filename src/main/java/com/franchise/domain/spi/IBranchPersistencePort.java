package com.franchise.domain.spi;

import com.franchise.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBranchPersistencePort {

    Mono<Branch> save(Branch branch);

    Mono<Branch> findById(String id);

    Flux<Branch> findByFranchiseId(String franchiseId);

    Mono<Branch> update(Branch branch);
}
