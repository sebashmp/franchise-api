package com.franchise.infrastructure.entrypoints.handler;

import com.franchise.domain.api.IBranchServicePort;
import com.franchise.domain.model.Branch;
import com.franchise.infrastructure.entrypoints.dto.BranchResponse;
import com.franchise.infrastructure.entrypoints.mapper.RestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchHandlerTest {

    @Mock private IBranchServicePort branchServicePort;
    @Mock private RestMapper restMapper;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        BranchHandler handler = new BranchHandler(branchServicePort, restMapper);
        webTestClient = WebTestClient.bindToRouterFunction(
                RouterFunctions.route()
                        .POST("/api/v1/franchises/{franchiseId}/branches", handler::addBranch)
                        .PATCH("/api/v1/branches/{branchId}", handler::updateBranchName)
                        .build()
        ).build();
    }

    @Test
    void addBranch_returns201WithBody() {
        Branch branch = Branch.builder().id("b-1").franchiseId("f-1").name("North").build();
        BranchResponse response = new BranchResponse("b-1", "f-1", "North");
        when(branchServicePort.addBranch("f-1", "North")).thenReturn(Mono.just(branch));
        when(restMapper.toResponse(branch)).thenReturn(response);

        webTestClient.post().uri("/api/v1/franchises/f-1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"North\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("b-1")
                .jsonPath("$.franchiseId").isEqualTo("f-1")
                .jsonPath("$.name").isEqualTo("North");
    }

    @Test
    void updateBranchName_returns200WithBody() {
        Branch branch = Branch.builder().id("b-1").franchiseId("f-1").name("Updated").build();
        BranchResponse response = new BranchResponse("b-1", "f-1", "Updated");
        when(branchServicePort.updateBranchName("b-1", "Updated")).thenReturn(Mono.just(branch));
        when(restMapper.toResponse(branch)).thenReturn(response);

        webTestClient.patch().uri("/api/v1/branches/b-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Updated\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated");
    }
}
