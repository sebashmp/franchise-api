package com.franchise.infrastructure.adapters.persistenceadapter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class BranchDocument {

    @Getter(onMethod_ = {@DynamoDbPartitionKey})
    private String id;

    @Getter(onMethod_ = {@DynamoDbSecondaryPartitionKey(indexNames = {"franchiseId-index"})})
    private String franchiseId;

    private String name;
}
