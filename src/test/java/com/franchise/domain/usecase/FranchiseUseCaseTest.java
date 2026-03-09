package com.franchise.domain.usecase;

import com.franchise.domain.exceptions.FranchiseNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;
import com.franchise.domain.model.TopStockResult;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IFranchisePersistencePort;
import com.franchise.domain.spi.IProductPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseUseCaseTest {

    @Mock private IFranchisePersistencePort franchisePersistencePort;
    @Mock private IBranchPersistencePort branchPersistencePort;
    @Mock private IProductPersistencePort productPersistencePort;

    private FranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FranchiseUseCase(franchisePersistencePort, branchPersistencePort, productPersistencePort);
    }

    // --- createFranchise ---

    @Test
    void createFranchise_whenValidName_returnsCreatedFranchise() {
        Franchise saved = Franchise.builder().id("f-1").name("Rappi").build();
        when(franchisePersistencePort.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.createFranchise("Rappi"))
                .assertNext(f -> {
                    assertThat(f.getId()).isEqualTo("f-1");
                    assertThat(f.getName()).isEqualTo("Rappi");
                })
                .verifyComplete();
    }

    @Test
    void createFranchise_whenBlankName_throwsInvalidNameException() {
        StepVerifier.create(useCase.createFranchise("  "))
                .expectError(InvalidNameException.class)
                .verify();
    }

    @Test
    void createFranchise_whenNullName_throwsInvalidNameException() {
        StepVerifier.create(useCase.createFranchise(null))
                .expectError(InvalidNameException.class)
                .verify();
    }

    // --- updateFranchiseName ---

    @Test
    void updateFranchiseName_whenValid_returnsUpdatedFranchise() {
        Franchise existing = Franchise.builder().id("f-1").name("Old Name").build();
        Franchise updated = Franchise.builder().id("f-1").name("New Name").build();
        when(franchisePersistencePort.findById("f-1")).thenReturn(Mono.just(existing));
        when(franchisePersistencePort.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.updateFranchiseName("f-1", "New Name"))
                .assertNext(f -> assertThat(f.getName()).isEqualTo("New Name"))
                .verifyComplete();
    }

    @Test
    void updateFranchiseName_whenBlankName_throwsInvalidNameException() {
        StepVerifier.create(useCase.updateFranchiseName("f-1", ""))
                .expectError(InvalidNameException.class)
                .verify();
    }

    @Test
    void updateFranchiseName_whenFranchiseNotFound_throwsFranchiseNotFoundException() {
        when(franchisePersistencePort.findById("f-99")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateFranchiseName("f-99", "New Name"))
                .expectError(FranchiseNotFoundException.class)
                .verify();
    }

    // --- getTopStockProductPerBranch ---

    @Test
    void getTopStockProductPerBranch_whenFranchiseExists_returnsTopProductPerBranch() {
        Franchise franchise = Franchise.builder().id("f-1").name("Rappi").build();
        Branch branch1 = Branch.builder().id("b-1").franchiseId("f-1").name("North").build();
        Branch branch2 = Branch.builder().id("b-2").franchiseId("f-1").name("South").build();
        Product p1 = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(100).build();
        Product p2 = Product.builder().id("p-2").branchId("b-1").name("Pizza").stock(50).build();
        Product p3 = Product.builder().id("p-3").branchId("b-2").name("Sushi").stock(200).build();

        when(franchisePersistencePort.findById("f-1")).thenReturn(Mono.just(franchise));
        when(branchPersistencePort.findByFranchiseId("f-1")).thenReturn(Flux.just(branch1, branch2));
        when(productPersistencePort.findByBranchId("b-1")).thenReturn(Flux.just(p1, p2));
        when(productPersistencePort.findByBranchId("b-2")).thenReturn(Flux.just(p3));

        StepVerifier.create(useCase.getTopStockProductPerBranch("f-1").collectList())
                .assertNext(results -> {
                    assertThat(results).hasSize(2);
                    TopStockResult r1 = results.stream()
                            .filter(r -> r.getBranchId().equals("b-1")).findFirst().orElseThrow();
                    assertThat(r1.getProduct().getStock()).isEqualTo(100);

                    TopStockResult r2 = results.stream()
                            .filter(r -> r.getBranchId().equals("b-2")).findFirst().orElseThrow();
                    assertThat(r2.getProduct().getStock()).isEqualTo(200);
                })
                .verifyComplete();
    }

    @Test
    void getTopStockProductPerBranch_whenBranchHasNoProducts_branchIsOmitted() {
        Franchise franchise = Franchise.builder().id("f-1").name("Rappi").build();
        Branch branch = Branch.builder().id("b-1").franchiseId("f-1").name("Empty Branch").build();

        when(franchisePersistencePort.findById("f-1")).thenReturn(Mono.just(franchise));
        when(branchPersistencePort.findByFranchiseId("f-1")).thenReturn(Flux.just(branch));
        when(productPersistencePort.findByBranchId("b-1")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.getTopStockProductPerBranch("f-1"))
                .verifyComplete();
    }

    @Test
    void getTopStockProductPerBranch_whenFranchiseNotFound_throwsFranchiseNotFoundException() {
        when(franchisePersistencePort.findById("f-99")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getTopStockProductPerBranch("f-99"))
                .expectError(FranchiseNotFoundException.class)
                .verify();
    }
}
