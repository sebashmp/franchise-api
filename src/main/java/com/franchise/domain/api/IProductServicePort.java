package com.franchise.domain.api;

import com.franchise.domain.model.Product;
import reactor.core.publisher.Mono;

public interface IProductServicePort {

    Mono<Product> addProduct(String branchId, String name, int stock);

    Mono<Void> removeProduct(String branchId, String productId);

    Mono<Product> updateProductStock(String productId, int newStock);

    Mono<Product> updateProductName(String productId, String newName);
}
