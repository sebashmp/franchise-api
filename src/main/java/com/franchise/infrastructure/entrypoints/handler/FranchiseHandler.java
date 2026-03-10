package com.franchise.infrastructure.entrypoints.handler;

import com.franchise.domain.api.IFranchiseServicePort;
import com.franchise.infrastructure.entrypoints.dto.CreateFranchiseRequest;
import com.franchise.infrastructure.entrypoints.dto.TopStockResponse;
import com.franchise.infrastructure.entrypoints.dto.UpdateNameRequest;
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
public class FranchiseHandler {

    private final IFranchiseServicePort franchiseServicePort;
    private final RestMapper restMapper;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        log.info("POST /franchises — creating franchise");
        return request.bodyToMono(CreateFranchiseRequest.class)
                .flatMap(body -> franchiseServicePort.createFranchise(body.getName()))
                .doOnNext(f -> log.info("Franchise created: id={} name={}", f.getId(), f.getName()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        log.info("PATCH /franchises/{} — updating name", franchiseId);
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(body -> franchiseServicePort.updateFranchiseName(franchiseId, body.getName()))
                .doOnNext(f -> log.info("Franchise updated: id={} newName={}", f.getId(), f.getName()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        log.info("GET /franchises/{}/top-products — fetching top stock per branch", franchiseId);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(franchiseServicePort.getTopStockProductPerBranch(franchiseId)
                        .map(restMapper::toResponse), TopStockResponse.class);
    }
}
