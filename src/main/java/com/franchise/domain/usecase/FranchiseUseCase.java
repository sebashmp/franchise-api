package com.franchise.domain.usecase;

import com.franchise.domain.api.IFranchiseServicePort;
import com.franchise.domain.exceptions.FranchiseNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.TopStockResult;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IFranchisePersistencePort;
import com.franchise.domain.spi.IProductPersistencePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

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
        return Mono.justOrEmpty(name)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Franchise name must not be blank.")))
                .map(n -> Franchise.builder()
                        .id(UUID.randomUUID().toString())
                        .name(n)
                        .build())
                .flatMap(franchisePersistencePort::save);
    }

    @Override
    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Franchise name must not be blank.")))
                .flatMap(n -> franchisePersistencePort.findById(franchiseId)
                        .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                        .map(franchise -> franchise.toBuilder().name(n).build()))
                .flatMap(franchisePersistencePort::update);
    }

    @Override
    public Flux<TopStockResult> getTopStockProductPerBranch(String franchiseId) {
        return franchisePersistencePort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMapMany(franchise -> branchPersistencePort.findByFranchiseId(franchiseId))
                .flatMap(branch -> productPersistencePort.findByBranchId(branch.getId())
                        .reduce((p1, p2) -> p1.getStock() >= p2.getStock() ? p1 : p2)
                        .map(topProduct -> TopStockResult.builder()
                                .branchId(branch.getId())
                                .branchName(branch.getName())
                                .product(topProduct)
                                .build()));
    }
}
