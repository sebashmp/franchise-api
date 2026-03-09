package com.franchise.domain.spi;

import com.franchise.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductPersistencePort {

    Mono<Product> save(Product product);

    Mono<Product> findById(String id);

    Mono<Product> findByIdAndBranchId(String id, String branchId);

    Flux<Product> findByBranchId(String branchId);

    Mono<Void> deleteByIdAndBranchId(String id, String branchId);

    Mono<Product> update(Product product);
}
