package com.franchise.domain.usecase;

import com.franchise.domain.api.IProductServicePort;
import com.franchise.domain.exceptions.BranchNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.exceptions.InvalidStockException;
import com.franchise.domain.exceptions.ProductNotFoundException;
import com.franchise.domain.model.Product;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IProductPersistencePort;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ProductUseCase implements IProductServicePort {

    private final IBranchPersistencePort branchPersistencePort;
    private final IProductPersistencePort productPersistencePort;

    public ProductUseCase(IBranchPersistencePort branchPersistencePort,
                          IProductPersistencePort productPersistencePort) {
        this.branchPersistencePort = branchPersistencePort;
        this.productPersistencePort = productPersistencePort;
    }

    @Override
    public Mono<Product> addProduct(String branchId, String name, int stock) {
        return Mono.justOrEmpty(name)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Product name must not be blank.")))
                .flatMap(n -> Mono.just(stock)
                        .filter(s -> s >= 0)
                        .switchIfEmpty(Mono.error(new InvalidStockException("Stock must be equal to or greater than 0.")))
                        .flatMap(s -> branchPersistencePort.findById(branchId)
                                .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
                                .map(branch -> Product.builder()
                                        .id(UUID.randomUUID().toString())
                                        .branchId(branchId)
                                        .name(n)
                                        .stock(s)
                                        .build())))
                .flatMap(productPersistencePort::save);
    }

    @Override
    public Mono<Void> removeProduct(String branchId, String productId) {
        return productPersistencePort.findByIdAndBranchId(productId, branchId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .flatMap(product -> productPersistencePort.deleteByIdAndBranchId(productId, branchId));
    }

    @Override
    public Mono<Product> updateProductStock(String productId, int newStock) {
        return Mono.just(newStock)
                .filter(s -> s >= 0)
                .switchIfEmpty(Mono.error(new InvalidStockException("Stock must be equal to or greater than 0.")))
                .flatMap(s -> productPersistencePort.findById(productId)
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                        .map(product -> product.toBuilder().stock(s).build()))
                .flatMap(productPersistencePort::update);
    }

    @Override
    public Mono<Product> updateProductName(String productId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Product name must not be blank.")))
                .flatMap(n -> productPersistencePort.findById(productId)
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                        .map(product -> product.toBuilder().name(n).build()))
                .flatMap(productPersistencePort::update);
    }
}
