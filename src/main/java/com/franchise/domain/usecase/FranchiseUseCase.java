package com.franchise.domain.usecase;

import com.franchise.domain.api.IFranchiseServicePort;
import com.franchise.domain.exceptions.FranchiseNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.TopStockResult;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IFranchisePersistencePort;
import com.franchise.domain.spi.IProductPersistencePort;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
public class FranchiseUseCase implements IFranchiseServicePort {

    private final IFranchisePersistencePort franchisePersistencePort;
    private final IBranchPersistencePort branchPersistencePort;
    private final IProductPersistencePort productPersistencePort;

    public FranchiseUseCase(IFranchisePersistencePort franchisePersistencePort,
                            IBranchPersistencePort branchPersistencePort,
                            IProductPersistencePort productPersistencePort) {
        this.franchisePersistencePort = franchisePersistencePort;
        this.branchPersistencePort = branchPersistencePort;
        this.productPersistencePort = productPersistencePort;
    }

    @Override
    public Mono<Franchise> createFranchise(String name) {
        log.debug("Validating franchise name: '{}'", name);
        return Mono.justOrEmpty(name)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Franchise name must not be blank.")))
                .doOnNext(n -> log.debug("Name validation passed for franchise: '{}'", n))
                .doOnError(InvalidNameException.class, e -> log.warn("Name validation failed: {}", e.getMessage()))
                .map(n -> Franchise.builder()
                        .id(UUID.randomUUID().toString())
                        .name(n)
                        .build())
                .doOnNext(f -> log.debug("Franchise entity built: id={}", f.getId()))
                .flatMap(franchisePersistencePort::save);
    }

    @Override
    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        log.debug("Validating new name '{}' for franchise id={}", newName, franchiseId);
        return Mono.justOrEmpty(newName)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Franchise name must not be blank.")))
                .doOnNext(n -> log.debug("Name validation passed, looking up franchise id={}", franchiseId))
                .doOnError(InvalidNameException.class, e -> log.warn("Name validation failed: {}", e.getMessage()))
                .flatMap(n -> franchisePersistencePort.findById(franchiseId)
                        .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                        .doOnNext(f -> log.debug("Franchise found: id={} currentName={}", f.getId(), f.getName()))
                        .doOnError(FranchiseNotFoundException.class, e -> log.warn("Franchise not found: id={}", franchiseId))
                        .map(franchise -> franchise.toBuilder().name(n).build()))
                .flatMap(franchisePersistencePort::update);
    }

    @Override
    public Flux<TopStockResult> getTopStockProductPerBranch(String franchiseId) {
        log.debug("Looking up franchise id={} for top-stock query", franchiseId);
        return franchisePersistencePort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .doOnNext(f -> log.debug("Franchise found: id={}, fetching branches", f.getId()))
                .doOnError(FranchiseNotFoundException.class, e -> log.warn("Franchise not found: id={}", franchiseId))
                .flatMapMany(franchise -> branchPersistencePort.findByFranchiseId(franchiseId))
                .doOnNext(b -> log.debug("Processing branch: id={} name={}", b.getId(), b.getName()))
                .flatMap(branch -> productPersistencePort.findByBranchId(branch.getId())
                        .reduce((p1, p2) -> p1.getStock() >= p2.getStock() ? p1 : p2)
                        .doOnNext(p -> log.debug("Top product for branch {}: id={} stock={}", branch.getName(), p.getId(), p.getStock()))
                        .map(topProduct -> TopStockResult.builder()
                                .branchId(branch.getId())
                                .branchName(branch.getName())
                                .product(topProduct)
                                .build()));
    }
}
