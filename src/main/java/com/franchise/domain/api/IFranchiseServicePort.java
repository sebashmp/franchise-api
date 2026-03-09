package com.franchise.domain.api;

import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.TopStockResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IFranchiseServicePort {

    Mono<Franchise> createFranchise(String name);

    Mono<Franchise> updateFranchiseName(String franchiseId, String newName);

    Flux<TopStockResult> getTopStockProductPerBranch(String franchiseId);
}
