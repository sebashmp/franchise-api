package com.franchise.infrastructure.adapters.persistenceadapter;

import com.franchise.domain.exceptions.DatabaseUnavailableException;
import com.franchise.domain.model.Branch;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.infrastructure.adapters.persistenceadapter.entity.BranchDocument;
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
public class BranchPersistenceAdapter implements IBranchPersistencePort {

    private final DynamoDbEnhancedAsyncClient client;
    private final DynamoDbMapper mapper;

    @Value("${aws.dynamodb.tables.branches}")
    private String tableName;

    private DynamoDbAsyncTable<BranchDocument> table;
    private DynamoDbAsyncIndex<BranchDocument> franchiseIdIndex;

    @PostConstruct
    void init() {
        this.table = client.table(tableName, TableSchema.fromBean(BranchDocument.class));
        this.franchiseIdIndex = table.index("franchiseId-index");
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "saveFallback")
    public Mono<Branch> save(Branch branch) {
        BranchDocument doc = mapper.toDocument(branch);
        return Mono.fromFuture(table.putItem(doc))
                .thenReturn(branch);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "findByIdFallback")
    public Mono<Branch> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Mono.fromFuture(table.getItem(key))
                .filter(Objects::nonNull)
                .map(mapper::toDomain);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "findByFranchiseIdFallback")
    public Flux<Branch> findByFranchiseId(String franchiseId) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(franchiseId).build()))
                .build();
        return Flux.from(franchiseIdIndex.query(request))
                .flatMap(page -> Flux.fromIterable(page.items()))
                .map(mapper::toDomain);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "updateFallback")
    public Mono<Branch> update(Branch branch) {
        BranchDocument doc = mapper.toDocument(branch);
        return Mono.fromFuture(table.updateItem(doc))
                .map(mapper::toDomain);
    }

    // --- Fallback methods ---

    public Mono<Branch> saveFallback(Branch branch, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Branch save operation is currently unavailable."));
    }

    public Mono<Branch> findByIdFallback(String id, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Branch lookup is currently unavailable."));
    }

    public Flux<Branch> findByFranchiseIdFallback(String franchiseId, Throwable t) {
        return Flux.error(new DatabaseUnavailableException("Branch listing is currently unavailable."));
    }

    public Mono<Branch> updateFallback(Branch branch, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Branch update operation is currently unavailable."));
    }
}
