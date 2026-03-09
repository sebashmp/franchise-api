package com.franchise.infrastructure.entrypoints.handler;

import com.franchise.domain.api.IFranchiseServicePort;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;
import com.franchise.domain.model.TopStockResult;
import com.franchise.infrastructure.entrypoints.dto.FranchiseResponse;
import com.franchise.infrastructure.entrypoints.dto.TopStockResponse;
import com.franchise.infrastructure.entrypoints.dto.ProductResponse;
import com.franchise.infrastructure.entrypoints.mapper.RestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseHandlerTest {

    @Mock private IFranchiseServicePort franchiseServicePort;
    @Mock private RestMapper restMapper;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        FranchiseHandler handler = new FranchiseHandler(franchiseServicePort, restMapper);
        webTestClient = WebTestClient.bindToRouterFunction(
                RouterFunctions.route()
                        .POST("/api/v1/franchises", handler::createFranchise)
                        .PATCH("/api/v1/franchises/{franchiseId}", handler::updateFranchiseName)
                        .GET("/api/v1/franchises/{franchiseId}/top-products", handler::getTopStockProducts)
                        .build()
        ).build();
    }

    @Test
    void createFranchise_returns201WithBody() {
        Franchise franchise = Franchise.builder().id("f-1").name("Rappi").build();
        FranchiseResponse response = new FranchiseResponse("f-1", "Rappi");
        when(franchiseServicePort.createFranchise("Rappi")).thenReturn(Mono.just(franchise));
        when(restMapper.toResponse(franchise)).thenReturn(response);

        webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Rappi\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("f-1")
                .jsonPath("$.name").isEqualTo("Rappi");
    }

    @Test
    void updateFranchiseName_returns200WithBody() {
        Franchise franchise = Franchise.builder().id("f-1").name("New Name").build();
        FranchiseResponse response = new FranchiseResponse("f-1", "New Name");
        when(franchiseServicePort.updateFranchiseName("f-1", "New Name")).thenReturn(Mono.just(franchise));
        when(restMapper.toResponse(franchise)).thenReturn(response);

        webTestClient.patch().uri("/api/v1/franchises/f-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Name\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("New Name");
    }

    @Test
    void getTopStockProducts_returns200WithList() {
        Product product = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(100).build();
        TopStockResult result = TopStockResult.builder()
                .branchId("b-1").branchName("North").product(product).build();
        ProductResponse productResponse = new ProductResponse("p-1", "b-1", "Burger", 100);
        TopStockResponse topStockResponse = new TopStockResponse("b-1", "North", productResponse);

        when(franchiseServicePort.getTopStockProductPerBranch("f-1")).thenReturn(Flux.just(result));
        when(restMapper.toResponse(result)).thenReturn(topStockResponse);

        webTestClient.get().uri("/api/v1/franchises/f-1/top-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].branchId").isEqualTo("b-1")
                .jsonPath("$[0].branchName").isEqualTo("North")
                .jsonPath("$[0].product.stock").isEqualTo(100);
    }
}
