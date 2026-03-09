package com.franchise.domain.usecase;

import com.franchise.domain.exceptions.BranchNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.exceptions.InvalidStockException;
import com.franchise.domain.exceptions.ProductNotFoundException;
import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Product;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IProductPersistencePort;
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
class ProductUseCaseTest {

    @Mock private IBranchPersistencePort branchPersistencePort;
    @Mock private IProductPersistencePort productPersistencePort;

    private ProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProductUseCase(branchPersistencePort, productPersistencePort);
    }

    // --- addProduct ---

    @Test
    void addProduct_whenValid_returnsCreatedProduct() {
        Branch branch = Branch.builder().id("b-1").franchiseId("f-1").name("North").build();
        Product saved = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(10).build();
        when(branchPersistencePort.findById("b-1")).thenReturn(Mono.just(branch));
        when(productPersistencePort.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.addProduct("b-1", "Burger", 10))
                .assertNext(p -> {
                    assertThat(p.getBranchId()).isEqualTo("b-1");
                    assertThat(p.getName()).isEqualTo("Burger");
                    assertThat(p.getStock()).isEqualTo(10);
                })
                .verifyComplete();
    }

    @Test
    void addProduct_whenZeroStock_succeeds() {
        Branch branch = Branch.builder().id("b-1").franchiseId("f-1").name("North").build();
        Product saved = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(0).build();
        when(branchPersistencePort.findById("b-1")).thenReturn(Mono.just(branch));
        when(productPersistencePort.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.addProduct("b-1", "Burger", 0))
                .assertNext(p -> assertThat(p.getStock()).isZero())
                .verifyComplete();
    }

    @Test
    void addProduct_whenBlankName_throwsInvalidNameException() {
        StepVerifier.create(useCase.addProduct("b-1", "  ", 10))
                .expectError(InvalidNameException.class)
                .verify();
    }

    @Test
    void addProduct_whenNegativeStock_throwsInvalidStockException() {
        StepVerifier.create(useCase.addProduct("b-1", "Burger", -1))
                .expectError(InvalidStockException.class)
                .verify();
    }

    @Test
    void addProduct_whenBranchNotFound_throwsBranchNotFoundException() {
        when(branchPersistencePort.findById("b-99")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.addProduct("b-99", "Burger", 10))
                .expectError(BranchNotFoundException.class)
                .verify();
    }

    // --- removeProduct ---

    @Test
    void removeProduct_whenProductExists_completesSuccessfully() {
        Product product = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(10).build();
        when(productPersistencePort.findByIdAndBranchId("p-1", "b-1")).thenReturn(Mono.just(product));
        when(productPersistencePort.deleteByIdAndBranchId("p-1", "b-1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.removeProduct("b-1", "p-1"))
                .verifyComplete();
    }

    @Test
    void removeProduct_whenProductNotFound_throwsProductNotFoundException() {
        when(productPersistencePort.findByIdAndBranchId("p-99", "b-1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.removeProduct("b-1", "p-99"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    // --- updateProductStock ---

    @Test
    void updateProductStock_whenValid_returnsUpdatedProduct() {
        Product existing = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(10).build();
        Product updated = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(50).build();
        when(productPersistencePort.findById("p-1")).thenReturn(Mono.just(existing));
        when(productPersistencePort.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.updateProductStock("p-1", 50))
                .assertNext(p -> assertThat(p.getStock()).isEqualTo(50))
                .verifyComplete();
    }

    @Test
    void updateProductStock_whenNegativeStock_throwsInvalidStockException() {
        StepVerifier.create(useCase.updateProductStock("p-1", -5))
                .expectError(InvalidStockException.class)
                .verify();
    }

    @Test
    void updateProductStock_whenProductNotFound_throwsProductNotFoundException() {
        when(productPersistencePort.findById("p-99")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateProductStock("p-99", 10))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    // --- updateProductName ---

    @Test
    void updateProductName_whenValid_returnsUpdatedProduct() {
        Product existing = Product.builder().id("p-1").branchId("b-1").name("Old").stock(10).build();
        Product updated = Product.builder().id("p-1").branchId("b-1").name("New").stock(10).build();
        when(productPersistencePort.findById("p-1")).thenReturn(Mono.just(existing));
        when(productPersistencePort.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.updateProductName("p-1", "New"))
                .assertNext(p -> assertThat(p.getName()).isEqualTo("New"))
                .verifyComplete();
    }

    @Test
    void updateProductName_whenBlankName_throwsInvalidNameException() {
        StepVerifier.create(useCase.updateProductName("p-1", ""))
                .expectError(InvalidNameException.class)
                .verify();
    }

    @Test
    void updateProductName_whenProductNotFound_throwsProductNotFoundException() {
        when(productPersistencePort.findById("p-99")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateProductName("p-99", "New"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }
}
