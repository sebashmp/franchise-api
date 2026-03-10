package com.franchise.domain.usecase;

import com.franchise.domain.api.IBranchServicePort;
import com.franchise.domain.exceptions.BranchNotFoundException;
import com.franchise.domain.exceptions.FranchiseNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.model.Branch;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IFranchisePersistencePort;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
public class BranchUseCase implements IBranchServicePort {

    private final IFranchisePersistencePort franchisePersistencePort;
    private final IBranchPersistencePort branchPersistencePort;

    public BranchUseCase(IFranchisePersistencePort franchisePersistencePort,
                         IBranchPersistencePort branchPersistencePort) {
        this.franchisePersistencePort = franchisePersistencePort;
        this.branchPersistencePort = branchPersistencePort;
    }

    @Override
    public Mono<Branch> addBranch(String franchiseId, String name) {
        log.debug("Validating branch name '{}' for franchiseId={}", name, franchiseId);
        return Mono.justOrEmpty(name)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Branch name must not be blank.")))
                .doOnNext(n -> log.debug("Name validation passed, looking up franchise id={}", franchiseId))
                .doOnError(InvalidNameException.class, e -> log.warn("Name validation failed: {}", e.getMessage()))
                .flatMap(n -> franchisePersistencePort.findById(franchiseId)
                        .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                        .doOnNext(f -> log.debug("Franchise found: id={}, building branch", f.getId()))
                        .doOnError(FranchiseNotFoundException.class, e -> log.warn("Franchise not found: id={}", franchiseId))
                        .map(franchise -> Branch.builder()
                                .id(UUID.randomUUID().toString())
                                .franchiseId(franchiseId)
                                .name(n)
                                .build()))
                .doOnNext(b -> log.debug("Branch entity built: id={}", b.getId()))
                .flatMap(branchPersistencePort::save);
    }

    @Override
    public Mono<Branch> updateBranchName(String branchId, String newName) {
        log.debug("Validating new name '{}' for branch id={}", newName, branchId);
        return Mono.justOrEmpty(newName)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Branch name must not be blank.")))
                .doOnNext(n -> log.debug("Name validation passed, looking up branch id={}", branchId))
                .doOnError(InvalidNameException.class, e -> log.warn("Name validation failed: {}", e.getMessage()))
                .flatMap(n -> branchPersistencePort.findById(branchId)
                        .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
                        .doOnNext(b -> log.debug("Branch found: id={} currentName={}", b.getId(), b.getName()))
                        .doOnError(BranchNotFoundException.class, e -> log.warn("Branch not found: id={}", branchId))
                        .map(branch -> branch.toBuilder().name(n).build()))
                .flatMap(branchPersistencePort::update);
    }
}
