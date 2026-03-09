package com.franchise.infrastructure.adapters.persistenceadapter.mapper;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;
import com.franchise.infrastructure.adapters.persistenceadapter.entity.BranchDocument;
import com.franchise.infrastructure.adapters.persistenceadapter.entity.FranchiseDocument;
import com.franchise.infrastructure.adapters.persistenceadapter.entity.ProductDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DynamoDbMapper {

    FranchiseDocument toDocument(Franchise franchise);
    Franchise toDomain(FranchiseDocument document);

    BranchDocument toDocument(Branch branch);
    Branch toDomain(BranchDocument document);

    ProductDocument toDocument(Product product);
    Product toDomain(ProductDocument document);
}
