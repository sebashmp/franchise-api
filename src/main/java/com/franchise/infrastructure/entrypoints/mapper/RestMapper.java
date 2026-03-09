package com.franchise.infrastructure.entrypoints.mapper;

import com.franchise.domain.model.Branch;
import com.franchise.domain.model.Franchise;
import com.franchise.domain.model.Product;
import com.franchise.domain.model.TopStockResult;
import com.franchise.infrastructure.entrypoints.dto.BranchResponse;
import com.franchise.infrastructure.entrypoints.dto.FranchiseResponse;
import com.franchise.infrastructure.entrypoints.dto.ProductResponse;
import com.franchise.infrastructure.entrypoints.dto.TopStockResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestMapper {

    FranchiseResponse toResponse(Franchise franchise);

    BranchResponse toResponse(Branch branch);

    ProductResponse toResponse(Product product);

    @Mapping(source = "branchName", target = "branchName")
    @Mapping(source = "product", target = "product")
    TopStockResponse toResponse(TopStockResult result);
}
