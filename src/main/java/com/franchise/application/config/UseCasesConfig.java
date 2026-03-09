package com.franchise.application.config;

import com.franchise.domain.api.IBranchServicePort;
import com.franchise.domain.api.IFranchiseServicePort;
import com.franchise.domain.api.IProductServicePort;
import com.franchise.domain.spi.IBranchPersistencePort;
import com.franchise.domain.spi.IFranchisePersistencePort;
import com.franchise.domain.spi.IProductPersistencePort;
import com.franchise.domain.usecase.BranchUseCase;
import com.franchise.domain.usecase.FranchiseUseCase;
import com.franchise.domain.usecase.ProductUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public IFranchiseServicePort franchiseServicePort(
            IFranchisePersistencePort franchisePersistencePort,
            IBranchPersistencePort branchPersistencePort,
            IProductPersistencePort productPersistencePort) {
        return new FranchiseUseCase(franchisePersistencePort, branchPersistencePort, productPersistencePort);
    }

    @Bean
    public IBranchServicePort branchServicePort(
            IFranchisePersistencePort franchisePersistencePort,
            IBranchPersistencePort branchPersistencePort) {
        return new BranchUseCase(franchisePersistencePort, branchPersistencePort);
    }

    @Bean
    public IProductServicePort productServicePort(
            IBranchPersistencePort branchPersistencePort,
            IProductPersistencePort productPersistencePort) {
        return new ProductUseCase(branchPersistencePort, productPersistencePort);
    }
}
