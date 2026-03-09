package com.franchise.infrastructure.entrypoints.handler;

import com.franchise.domain.api.IProductServicePort;
import com.franchise.domain.model.Product;
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
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductHandlerTest {

    @Mock private IProductServicePort productServicePort;
    @Mock private RestMapper restMapper;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        ProductHandler handler = new ProductHandler(productServicePort, restMapper);
        webTestClient = WebTestClient.bindToRouterFunction(
                RouterFunctions.route()
                        .POST("/api/v1/branches/{branchId}/products", handler::addProduct)
                        .DELETE("/api/v1/branches/{branchId}/products/{productId}", handler::removeProduct)
                        .PATCH("/api/v1/products/{productId}/stock", handler::updateProductStock)
                        .PATCH("/api/v1/products/{productId}", handler::updateProductName)
                        .build()
        ).build();
    }

    @Test
    void addProduct_returns201WithBody() {
        Product product = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(10).build();
        ProductResponse response = new ProductResponse("p-1", "b-1", "Burger", 10);
        when(productServicePort.addProduct("b-1", "Burger", 10)).thenReturn(Mono.just(product));
        when(restMapper.toResponse(product)).thenReturn(response);

        webTestClient.post().uri("/api/v1/branches/b-1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger\",\"stock\":10}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("p-1")
                .jsonPath("$.name").isEqualTo("Burger")
                .jsonPath("$.stock").isEqualTo(10);
    }

    @Test
    void removeProduct_returns204() {
        when(productServicePort.removeProduct("b-1", "p-1")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/branches/b-1/products/p-1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void updateProductStock_returns200WithBody() {
        Product product = Product.builder().id("p-1").branchId("b-1").name("Burger").stock(50).build();
        ProductResponse response = new ProductResponse("p-1", "b-1", "Burger", 50);
        when(productServicePort.updateProductStock("p-1", 50)).thenReturn(Mono.just(product));
        when(restMapper.toResponse(product)).thenReturn(response);

        webTestClient.patch().uri("/api/v1/products/p-1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":50}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(50);
    }

    @Test
    void updateProductName_returns200WithBody() {
        Product product = Product.builder().id("p-1").branchId("b-1").name("New Name").stock(10).build();
        ProductResponse response = new ProductResponse("p-1", "b-1", "New Name", 10);
        when(productServicePort.updateProductName("p-1", "New Name")).thenReturn(Mono.just(product));
        when(restMapper.toResponse(product)).thenReturn(response);

        webTestClient.patch().uri("/api/v1/products/p-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Name\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("New Name");
    }
}
