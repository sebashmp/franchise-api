package com.franchise.infrastructure.entrypoints;

import com.franchise.infrastructure.entrypoints.handler.BranchHandler;
import com.franchise.infrastructure.entrypoints.handler.FranchiseHandler;
import com.franchise.infrastructure.entrypoints.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterRest {

    @Bean
    public RouterFunction<ServerResponse> routes(FranchiseHandler franchiseHandler,
                                                  BranchHandler branchHandler,
                                                  ProductHandler productHandler) {
        return RouterFunctions.route()
                .path("/api/v1", builder -> builder
                        .path("/franchises", fb -> fb
                                .POST("", franchiseHandler::createFranchise)
                                .GET("/{franchiseId}/top-products", franchiseHandler::getTopStockProducts)
                                .PATCH("/{franchiseId}", franchiseHandler::updateFranchiseName)
                                .POST("/{franchiseId}/branches", branchHandler::addBranch))
                        .path("/branches", bb -> bb
                                .POST("/{branchId}/products", productHandler::addProduct)
                                .DELETE("/{branchId}/products/{productId}", productHandler::removeProduct)
                                .PATCH("/{branchId}", branchHandler::updateBranchName))
                        .path("/products", pb -> pb
                                .PATCH("/{productId}/stock", productHandler::updateProductStock)
                                .PATCH("/{productId}", productHandler::updateProductName)))
                .build();
    }
}
