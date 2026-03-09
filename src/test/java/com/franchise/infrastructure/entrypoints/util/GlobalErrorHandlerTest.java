package com.franchise.infrastructure.entrypoints.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.franchise.domain.exceptions.BranchNotFoundException;
import com.franchise.domain.exceptions.DatabaseUnavailableException;
import com.franchise.domain.exceptions.FranchiseNotFoundException;
import com.franchise.domain.exceptions.InvalidNameException;
import com.franchise.domain.exceptions.InvalidStockException;
import com.franchise.domain.exceptions.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalErrorHandlerTest {

    private GlobalErrorHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalErrorHandler(new ObjectMapper());
    }

    @Test
    void handle_whenFranchiseNotFound_returns404() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/franchises/f-99").build());

        handler.handle(exchange, new FranchiseNotFoundException("f-99")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handle_whenBranchNotFound_returns404() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/branches/b-99").build());

        handler.handle(exchange, new BranchNotFoundException("b-99")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handle_whenProductNotFound_returns404() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/products/p-99").build());

        handler.handle(exchange, new ProductNotFoundException("p-99")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handle_whenInvalidName_returns400() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/franchises").build());

        handler.handle(exchange, new InvalidNameException("Franchise name must not be blank.")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handle_whenInvalidStock_returns400() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.patch("/api/v1/products/p-1/stock").build());

        handler.handle(exchange, new InvalidStockException("Stock must be >= 0.")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handle_whenDatabaseUnavailable_returns503() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/franchises/f-1").build());

        handler.handle(exchange, new DatabaseUnavailableException("DB down.")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handle_whenUnknownException_returns500() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/franchises/f-1").build());

        handler.handle(exchange, new RuntimeException("Unexpected error")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
