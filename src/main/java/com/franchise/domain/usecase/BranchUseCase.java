package com.franchise.domain.usecase;

import com.franchise.domain.api.IBranchServicePort;
import com.franchise.domain.exceptions.BranchNotFoundException;
import com.franchise.domain.exceptions.FranchiseNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.model.Branch;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IFranchisePersistencePort;
import reactor.core.publisher.Mono;

import java.util.UUID;

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
        return Mono.justOrEmpty(name)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Branch name must not be blank.")))
                .flatMap(n -> franchisePersistencePort.findById(franchiseId)
                        .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                        .map(franchise -> Branch.builder()
                                .id(UUID.randomUUID().toString())
                                .franchiseId(franchiseId)
                                .name(n)
                                .build()))
                .flatMap(branchPersistencePort::save);
    }

    @Override
    public Mono<Branch> updateBranchName(String branchId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Branch name must not be blank.")))
                .flatMap(n -> branchPersistencePort.findById(branchId)
                        .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
                        .map(branch -> branch.toBuilder().name(n).build()))
                .flatMap(branchPersistencePort::update);
    }
}
