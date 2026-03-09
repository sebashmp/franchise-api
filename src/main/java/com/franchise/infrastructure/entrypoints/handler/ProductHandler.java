package com.franchise.infrastructure.entrypoints.handler;

import com.franchise.domain.api.IProductServicePort;
import com.franchise.infrastructure.entrypoints.dto.AddProductRequest;
import com.franchise.infrastructure.entrypoints.dto.UpdateNameRequest;
import com.franchise.infrastructure.entrypoints.dto.UpdateStockRequest;
import com.franchise.infrastructure.entrypoints.mapper.RestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final IProductServicePort productServicePort;
    private final RestMapper restMapper;

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        String branchId = request.pathVariable("branchId");
        return request.bodyToMono(AddProductRequest.class)
                .flatMap(body -> productServicePort.addProduct(branchId, body.getName(), body.getStock()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");
        return productServicePort.removeProduct(branchId, productId)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateProductStock(ServerRequest request) {
        String productId = request.pathVariable("productId");
        return request.bodyToMono(UpdateStockRequest.class)
                .flatMap(body -> productServicePort.updateProductStock(productId, body.getStock()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        String productId = request.pathVariable("productId");
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(body -> productServicePort.updateProductName(productId, body.getName()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}
