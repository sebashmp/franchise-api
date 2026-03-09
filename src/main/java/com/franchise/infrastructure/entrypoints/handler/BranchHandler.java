package com.franchise.infrastructure.entrypoints.handler;

import com.franchise.domain.api.IBranchServicePort;
import com.franchise.infrastructure.entrypoints.dto.AddBranchRequest;
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
public class BranchHandler {

    private final IBranchServicePort branchServicePort;
    private final RestMapper restMapper;

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return request.bodyToMono(AddBranchRequest.class)
                .flatMap(body -> branchServicePort.addBranch(franchiseId, body.getName()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        String branchId = request.pathVariable("branchId");
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(body -> branchServicePort.updateBranchName(branchId, body.getName()))
                .map(restMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}
