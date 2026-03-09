package com.franchise.infrastructure.entrypoints.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franchise.domain.exceptions.DomainException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        ErrorDTO errorDTO;

        if (ex instanceof DomainException domainEx) {
            status = resolveStatus(domainEx.getErrorCode());
            errorDTO = new ErrorDTO(domainEx.getErrorCode(), domainEx.getMessage());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorDTO = new ErrorDTO("INTERNAL_ERROR", "An unexpected error occurred.");
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorDTO);
        } catch (JsonProcessingException e) {
            bytes = "{\"errorCode\":\"INTERNAL_ERROR\",\"message\":\"An unexpected error occurred.\"}".getBytes();
        }

        var buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private HttpStatus resolveStatus(String errorCode) {
        return switch (errorCode) {
            case "FRANCHISE_NOT_FOUND", "BRANCH_NOT_FOUND", "PRODUCT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INVALID_NAME", "INVALID_STOCK" -> HttpStatus.BAD_REQUEST;
            case "DATABASE_UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
