package com.franchise.domain.usecase;

import com.franchise.domain.api.IProductServicePort;
import com.franchise.domain.exceptions.BranchNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.exceptions.InvalidStockException;
import com.franchise.domain.exceptions.ProductNotFoundException;
import com.franchise.domain.model.Product;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IProductPersistencePort;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
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
        log.debug("Validating product name='{}' stock={} for branchId={}", name, stock, branchId);
        return Mono.justOrEmpty(name)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Product name must not be blank.")))
                .doOnNext(n -> log.debug("Name validation passed: '{}'", n))
                .doOnError(InvalidNameException.class, e -> log.warn("Name validation failed: {}", e.getMessage()))
                .flatMap(n -> Mono.just(stock)
                        .filter(s -> s >= 0)
                        .switchIfEmpty(Mono.error(new InvalidStockException("Stock must be equal to or greater than 0.")))
                        .doOnNext(s -> log.debug("Stock validation passed: {}", s))
                        .doOnError(InvalidStockException.class, e -> log.warn("Stock validation failed: {}", e.getMessage()))
                        .flatMap(s -> branchPersistencePort.findById(branchId)
                                .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
                                .doOnNext(b -> log.debug("Branch found: id={}, building product", b.getId()))
                                .doOnError(BranchNotFoundException.class, e -> log.warn("Branch not found: id={}", branchId))
                                .map(branch -> Product.builder()
                                        .id(UUID.randomUUID().toString())
                                        .branchId(branchId)
                                        .name(n)
                                        .stock(s)
                                        .build())))
                .doOnNext(p -> log.debug("Product entity built: id={}", p.getId()))
                .flatMap(productPersistencePort::save);
    }

    @Override
    public Mono<Void> removeProduct(String branchId, String productId) {
        log.debug("Looking up product id={} in branchId={} for removal", productId, branchId);
        return productPersistencePort.findByIdAndBranchId(productId, branchId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .doOnNext(p -> log.debug("Product found: id={} name={}, proceeding with deletion", p.getId(), p.getName()))
                .doOnError(ProductNotFoundException.class, e -> log.warn("Product not found: id={} in branchId={}", productId, branchId))
                .flatMap(product -> productPersistencePort.deleteByIdAndBranchId(productId, branchId));
    }

    @Override
    public Mono<Product> updateProductStock(String productId, int newStock) {
        log.debug("Validating new stock={} for product id={}", newStock, productId);
        return Mono.just(newStock)
                .filter(s -> s >= 0)
                .switchIfEmpty(Mono.error(new InvalidStockException("Stock must be equal to or greater than 0.")))
                .doOnNext(s -> log.debug("Stock validation passed: {}, looking up product id={}", s, productId))
                .doOnError(InvalidStockException.class, e -> log.warn("Stock validation failed: {}", e.getMessage()))
                .flatMap(s -> productPersistencePort.findById(productId)
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                        .doOnNext(p -> log.debug("Product found: id={} currentStock={}", p.getId(), p.getStock()))
                        .doOnError(ProductNotFoundException.class, e -> log.warn("Product not found: id={}", productId))
                        .map(product -> product.toBuilder().stock(s).build()))
                .flatMap(productPersistencePort::update);
    }

    @Override
    public Mono<Product> updateProductName(String productId, String newName) {
        log.debug("Validating new name '{}' for product id={}", newName, productId);
        return Mono.justOrEmpty(newName)
                .filter(n -> !n.isBlank())
                .switchIfEmpty(Mono.error(new InvalidNameException("Product name must not be blank.")))
                .doOnNext(n -> log.debug("Name validation passed, looking up product id={}", productId))
                .doOnError(InvalidNameException.class, e -> log.warn("Name validation failed: {}", e.getMessage()))
                .flatMap(n -> productPersistencePort.findById(productId)
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                        .doOnNext(p -> log.debug("Product found: id={} currentName={}", p.getId(), p.getName()))
                        .doOnError(ProductNotFoundException.class, e -> log.warn("Product not found: id={}", productId))
                        .map(product -> product.toBuilder().name(n).build()))
                .flatMap(productPersistencePort::update);
    }
}
