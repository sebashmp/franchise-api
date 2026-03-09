package com.franchise.infrastructure.entrypoints.handler;

import com.franchise.domain.api.IFranchiseServicePort;
import com.franchise.infrastructure.entrypoints.dto.CreateFranchiseRequest;
import com.franchise.infrastructure.entrypoints.dto.TopStockResponse;
import com.franchise.infrastructure.entrypoints.dto.UpdateNameRequest;
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
public class FranchiseHandler {

    private final IFranchiseServicePort franchiseServicePort;
    private final RestMapper restMapper;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
                .flatMap(body -> franchiseServicePort.createFranchise(body.getName()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(body -> franchiseServicePort.updateFranchiseName(franchiseId, body.getName()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(franchiseServicePort.getTopStockProductPerBranch(franchiseId)
                        .map(restMapper::toResponse), TopStockResponse.class);
    }
}
