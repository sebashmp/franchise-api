package com.franchise.infrastructure.entrypoints.handler;

import com.franchise.domain.api.IProductServicePort;
import com.franchise.infrastructure.entrypoints.dto.AddProductRequest;
import com.franchise.infrastructure.entrypoints.dto.UpdateNameRequest;
import com.franchise.infrastructure.entrypoints.dto.UpdateStockRequest;
import com.franchise.infrastructure.entrypoints.mapper.RestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final IProductServicePort productServicePort;
    private final RestMapper restMapper;

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        String branchId = request.pathVariable("branchId");
        log.info("POST /branches/{}/products — adding product", branchId);
        return request.bodyToMono(AddProductRequest.class)
                .flatMap(body -> productServicePort.addProduct(branchId, body.getName(), body.getStock()))
                .doOnNext(p -> log.info("Product created: id={} name={} stock={} branchId={}", p.getId(), p.getName(), p.getStock(), p.getBranchId()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");
        log.info("DELETE /branches/{}/products/{} — removing product", branchId, productId);
        return productServicePort.removeProduct(branchId, productId)
                .doOnSuccess(v -> log.info("Product removed: id={} from branchId={}", productId, branchId))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateProductStock(ServerRequest request) {
        String productId = request.pathVariable("productId");
        log.info("PATCH /products/{}/stock — updating stock", productId);
        return request.bodyToMono(UpdateStockRequest.class)
                .flatMap(body -> productServicePort.updateProductStock(productId, body.getStock()))
                .doOnNext(p -> log.info("Product stock updated: id={} newStock={}", p.getId(), p.getStock()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        String productId = request.pathVariable("productId");
        log.info("PATCH /products/{} — updating name", productId);
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(body -> productServicePort.updateProductName(productId, body.getName()))
                .doOnNext(p -> log.info("Product name updated: id={} newName={}", p.getId(), p.getName()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}
