package com.franchise.infrastructure.entrypoints;

import com.franchise.infrastructure.entrypoints.dto.AddBranchRequest;
import com.franchise.infrastructure.entrypoints.dto.AddProductRequest;
import com.franchise.infrastructure.entrypoints.dto.BranchResponse;
import com.franchise.infrastructure.entrypoints.dto.CreateFranchiseRequest;
import com.franchise.infrastructure.entrypoints.dto.FranchiseResponse;
import com.franchise.infrastructure.entrypoints.dto.ProductResponse;
import com.franchise.infrastructure.entrypoints.dto.TopStockResponse;
import com.franchise.infrastructure.entrypoints.dto.UpdateNameRequest;
import com.franchise.infrastructure.entrypoints.dto.UpdateStockRequest;
import com.franchise.infrastructure.entrypoints.handler.BranchHandler;
import com.franchise.infrastructure.entrypoints.handler.FranchiseHandler;
import com.franchise.infrastructure.entrypoints.handler.ProductHandler;
import com.franchise.infrastructure.entrypoints.util.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/franchises",
                    method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "createFranchise",
                    operation = @Operation(
                            operationId = "createFranchise",
                            summary = "Create a new franchise",
                            tags = {"Franchise"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = CreateFranchiseRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franchise created",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}",
                    method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "updateFranchiseName",
                    operation = @Operation(
                            operationId = "updateFranchiseName",
                            summary = "Update franchise name",
                            tags = {"Franchise"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = UpdateNameRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franchise updated",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}/top-products",
                    method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "getTopStockProducts",
                    operation = @Operation(
                            operationId = "getTopStockProducts",
                            summary = "Get top-stock product per branch for a franchise",
                            tags = {"Franchise"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Top-stock products per branch",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopStockResponse.class)))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}/branches",
                    method = RequestMethod.POST,
                    beanClass = BranchHandler.class,
                    beanMethod = "addBranch",
                    operation = @Operation(
                            operationId = "addBranch",
                            summary = "Add a branch to a franchise",
                            tags = {"Branch"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = AddBranchRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Branch created",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}",
                    method = RequestMethod.PATCH,
                    beanClass = BranchHandler.class,
                    beanMethod = "updateBranchName",
                    operation = @Operation(
                            operationId = "updateBranchName",
                            summary = "Update branch name",
                            tags = {"Branch"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = UpdateNameRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch updated",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}/products",
                    method = RequestMethod.POST,
                    beanClass = ProductHandler.class,
                    beanMethod = "addProduct",
                    operation = @Operation(
                            operationId = "addProduct",
                            summary = "Add a product to a branch",
                            tags = {"Product"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = AddProductRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Product created",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name or stock",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}/products/{productId}",
                    method = RequestMethod.DELETE,
                    beanClass = ProductHandler.class,
                    beanMethod = "removeProduct",
                    operation = @Operation(
                            operationId = "removeProduct",
                            summary = "Remove a product from a branch",
                            tags = {"Product"},
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Product removed"),
                                    @ApiResponse(responseCode = "404", description = "Product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/products/{productId}/stock",
                    method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class,
                    beanMethod = "updateProductStock",
                    operation = @Operation(
                            operationId = "updateProductStock",
                            summary = "Update product stock",
                            tags = {"Product"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = UpdateStockRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product updated",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid stock",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/products/{productId}",
                    method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class,
                    beanMethod = "updateProductName",
                    operation = @Operation(
                            operationId = "updateProductName",
                            summary = "Update product name",
                            tags = {"Product"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = UpdateNameRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product updated",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
                            }
                    )
            )
    })
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
