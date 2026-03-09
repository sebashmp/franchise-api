package com.franchise.domain.usecase;

import com.franchise.domain.exceptions.BranchNotFoundException;
import com.franchise.domain.exceptions.FranchiseNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IFranchisePersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchUseCaseTest {

    @Mock private IFranchisePersistencePort franchisePersistencePort;
    @Mock private IBranchPersistencePort branchPersistencePort;

    private BranchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BranchUseCase(franchisePersistencePort, branchPersistencePort);
    }

    // --- addBranch ---

    @Test
    void addBranch_whenValid_returnsCreatedBranch() {
        Franchise franchise = Franchise.builder().id("f-1").name("Rappi").build();
        Branch saved = Branch.builder().id("b-1").franchiseId("f-1").name("North").build();
        when(franchisePersistencePort.findById("f-1")).thenReturn(Mono.just(franchise));
        when(branchPersistencePort.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.addBranch("f-1", "North"))
                .assertNext(b -> {
                    assertThat(b.getId()).isEqualTo("b-1");
                    assertThat(b.getFranchiseId()).isEqualTo("f-1");
                    assertThat(b.getName()).isEqualTo("North");
                })
                .verifyComplete();
    }

    @Test
    void addBranch_whenBlankName_throwsInvalidNameException() {
        StepVerifier.create(useCase.addBranch("f-1", "   "))
                .expectError(InvalidNameException.class)
                .verify();
    }

    @Test
    void addBranch_whenNullName_throwsInvalidNameException() {
        StepVerifier.create(useCase.addBranch("f-1", null))
                .expectError(InvalidNameException.class)
                .verify();
    }

    @Test
    void addBranch_whenFranchiseNotFound_throwsFranchiseNotFoundException() {
        when(franchisePersistencePort.findById("f-99")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.addBranch("f-99", "North"))
                .expectError(FranchiseNotFoundException.class)
                .verify();
    }

    // --- updateBranchName ---

    @Test
    void updateBranchName_whenValid_returnsUpdatedBranch() {
        Branch existing = Branch.builder().id("b-1").franchiseId("f-1").name("Old").build();
        Branch updated = Branch.builder().id("b-1").franchiseId("f-1").name("New").build();
        when(branchPersistencePort.findById("b-1")).thenReturn(Mono.just(existing));
        when(branchPersistencePort.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.updateBranchName("b-1", "New"))
                .assertNext(b -> assertThat(b.getName()).isEqualTo("New"))
                .verifyComplete();
    }

    @Test
    void updateBranchName_whenBlankName_throwsInvalidNameException() {
        StepVerifier.create(useCase.updateBranchName("b-1", ""))
                .expectError(InvalidNameException.class)
                .verify();
    }

    @Test
    void updateBranchName_whenBranchNotFound_throwsBranchNotFoundException() {
        when(branchPersistencePort.findById("b-99")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateBranchName("b-99", "New"))
                .expectError(BranchNotFoundException.class)
                .verify();
    }
}
