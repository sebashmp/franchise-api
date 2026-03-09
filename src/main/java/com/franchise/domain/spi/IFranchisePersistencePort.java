package com.franchise.domain.spi;

import com.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface IFranchisePersistencePort {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(String id);

    Mono<Franchise> update(Franchise franchise);
}
