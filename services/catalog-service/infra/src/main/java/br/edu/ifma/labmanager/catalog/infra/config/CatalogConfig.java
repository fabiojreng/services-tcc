package br.edu.ifma.labmanager.catalog.infra.config;

import br.edu.ifma.labmanager.catalog.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.catalog.application.use_cases.GetLaboratoryUseCase;
import br.edu.ifma.labmanager.catalog.application.use_cases.RegisterLaboratoryUseCase;
import br.edu.ifma.labmanager.catalog.domain.repository.LaboratoryRepository;
import br.edu.ifma.labmanager.catalog.infra.repositories.JpaLaboratoryRepository;
import br.edu.ifma.labmanager.catalog.infra.repositories.SpringDataLaboratoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CatalogConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    LaboratoryRepository laboratoryRepository(SpringDataLaboratoryRepository springData) {
        return new JpaLaboratoryRepository(springData);
    }

    @Bean
    RegisterLaboratoryUseCase registerLaboratoryUseCase(
            LaboratoryRepository laboratoryRepository,
            IdentityGateway identityGateway
    ) {
        return new RegisterLaboratoryUseCase(laboratoryRepository, identityGateway);
    }

    @Bean
    GetLaboratoryUseCase getLaboratoryUseCase(LaboratoryRepository laboratoryRepository) {
        return new GetLaboratoryUseCase(laboratoryRepository);
    }
}
