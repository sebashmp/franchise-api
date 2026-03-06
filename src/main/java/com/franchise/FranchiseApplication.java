package com.franchise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class FranchiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(FranchiseApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Enable automatic MDC context propagation through reactive chains
        // Required for traceId/spanId to appear in logs across flatMap boundaries
        Hooks.enableAutomaticContextPropagation();
    }
}
