package com.franchise.infrastructure.adapters.persistenceadapter;

import com.franchise.domain.exceptions.DatabaseUnavailableException;
import com.franchise.domain.model.Product;
import com.franchise.domain.spi.IProductPersistencePort;
import com.franchise.infrastructure.adapters.persistenceadapter.entity.ProductDocument;
import com.franchise.infrastructure.adapters.persistenceadapter.mapper.DynamoDbMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements IProductPersistencePort {

    private final DynamoDbEnhancedAsyncClient client;
    private final DynamoDbMapper mapper;

    @Value("${aws.dynamodb.tables.products}")
    private String tableName;

    private DynamoDbAsyncTable<ProductDocument> table;
    private DynamoDbAsyncIndex<ProductDocument> branchIdIndex;

    @PostConstruct
    void init() {
        this.table = client.table(tableName, TableSchema.fromBean(ProductDocument.class));
        this.branchIdIndex = table.index("branchId-index");
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "saveFallback")
    public Mono<Product> save(Product product) {
        ProductDocument doc = mapper.toDocument(product);
        return Mono.fromFuture(table.putItem(doc))
                .thenReturn(product);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "findByIdFallback")
    public Mono<Product> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Mono.fromFuture(table.getItem(key))
                .filter(Objects::nonNull)
                .map(mapper::toDomain);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "findByIdAndBranchIdFallback")
    public Mono<Product> findByIdAndBranchId(String productId, String branchId) {
        Key key = Key.builder().partitionValue(productId).build();
        return Mono.fromFuture(table.getItem(key))
                .filter(Objects::nonNull)
                .filter(doc -> branchId.equals(doc.getBranchId()))
                .map(mapper::toDomain);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "findByBranchIdFallback")
    public Flux<Product> findByBranchId(String branchId) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(branchId).build()))
                .build();
        return Flux.from(branchIdIndex.query(request))
                .flatMap(page -> Flux.fromIterable(page.items()))
                .map(mapper::toDomain);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "deleteByIdAndBranchIdFallback")
    public Mono<Void> deleteByIdAndBranchId(String productId, String branchId) {
        Key key = Key.builder().partitionValue(productId).build();
        return Mono.fromFuture(table.deleteItem(key)).then();
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "updateFallback")
    public Mono<Product> update(Product product) {
        ProductDocument doc = mapper.toDocument(product);
        return Mono.fromFuture(table.updateItem(doc))
                .map(mapper::toDomain);
    }

    // --- Fallback methods ---

    public Mono<Product> saveFallback(Product product, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Product save operation is currently unavailable."));
    }

    public Mono<Product> findByIdFallback(String id, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Product lookup is currently unavailable."));
    }

    public Mono<Product> findByIdAndBranchIdFallback(String productId, String branchId, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Product lookup is currently unavailable."));
    }

    public Flux<Product> findByBranchIdFallback(String branchId, Throwable t) {
        return Flux.error(new DatabaseUnavailableException("Product listing is currently unavailable."));
    }

    public Mono<Void> deleteByIdAndBranchIdFallback(String productId, String branchId, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Product delete operation is currently unavailable."));
    }

    public Mono<Product> updateFallback(Product product, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Product update operation is currently unavailable."));
    }
}
