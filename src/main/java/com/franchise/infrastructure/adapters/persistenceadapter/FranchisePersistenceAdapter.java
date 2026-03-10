package com.franchise.infrastructure.adapters.persistenceadapter;

import com.franchise.domain.exceptions.DatabaseUnavailableException;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.spi.IFranchisePersistencePort;
import com.franchise.infrastructure.adapters.persistenceadapter.entity.FranchiseDocument;
import com.franchise.infrastructure.adapters.persistenceadapter.mapper.DynamoDbMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FranchisePersistenceAdapter implements IFranchisePersistencePort {

    private final DynamoDbEnhancedAsyncClient client;
    private final DynamoDbMapper mapper;

    @Value("${aws.dynamodb.tables.franchises}")
    private String tableName;

    private DynamoDbAsyncTable<FranchiseDocument> table;

    @PostConstruct
    void init() {
        this.table = client.table(tableName, TableSchema.fromBean(FranchiseDocument.class));
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "saveFallback")
    public Mono<Franchise> save(Franchise franchise) {
        FranchiseDocument doc = mapper.toDocument(franchise);
        return Mono.fromFuture(table.putItem(doc))
                .thenReturn(franchise);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "findByIdFallback")
    public Mono<Franchise> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Mono.fromFuture(table.getItem(key))
                .filter(Objects::nonNull)
                .map(mapper::toDomain);
    }

    @Override
    @CircuitBreaker(name = "dynamodb", fallbackMethod = "updateFallback")
    public Mono<Franchise> update(Franchise franchise) {
        FranchiseDocument doc = mapper.toDocument(franchise);
        return Mono.fromFuture(table.updateItem(doc))
                .map(mapper::toDomain);
    }

    // --- Fallback methods ---

    public Mono<Franchise> saveFallback(Franchise franchise, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Franchise save operation is currently unavailable."));
    }

    public Mono<Franchise> findByIdFallback(String id, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Franchise lookup is currently unavailable."));
    }

    public Mono<Franchise> updateFallback(Franchise franchise, Throwable t) {
        return Mono.error(new DatabaseUnavailableException("Franchise update operation is currently unavailable."));
    }
}
