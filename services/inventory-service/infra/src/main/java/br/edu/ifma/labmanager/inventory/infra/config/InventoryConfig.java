package br.edu.ifma.labmanager.inventory.infra.config;

import br.edu.ifma.labmanager.inventory.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.inventory.application.use_cases.GetBalanceUseCase;
import br.edu.ifma.labmanager.inventory.application.use_cases.RegisterItemUseCase;
import br.edu.ifma.labmanager.inventory.application.use_cases.RegisterMovementUseCase;
import br.edu.ifma.labmanager.inventory.domain.repository.ItemRepository;
import br.edu.ifma.labmanager.inventory.domain.repository.MovementRepository;
import br.edu.ifma.labmanager.inventory.infra.repositories.JpaItemRepository;
import br.edu.ifma.labmanager.inventory.infra.repositories.JpaMovementRepository;
import br.edu.ifma.labmanager.inventory.infra.repositories.SpringDataItemRepository;
import br.edu.ifma.labmanager.inventory.infra.repositories.SpringDataMovementRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class InventoryConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    ItemRepository itemRepository(SpringDataItemRepository springData) {
        return new JpaItemRepository(springData);
    }

    @Bean
    MovementRepository movementRepository(SpringDataMovementRepository springData) {
        return new JpaMovementRepository(springData);
    }

    @Bean
    RegisterItemUseCase registerItemUseCase(ItemRepository itemRepository, IdentityGateway identityGateway) {
        return new RegisterItemUseCase(itemRepository, identityGateway);
    }

    @Bean
    RegisterMovementUseCase registerMovementUseCase(
            ItemRepository itemRepository,
            MovementRepository movementRepository,
            IdentityGateway identityGateway
    ) {
        return new RegisterMovementUseCase(itemRepository, movementRepository, identityGateway);
    }

    @Bean
    GetBalanceUseCase getBalanceUseCase(ItemRepository itemRepository, MovementRepository movementRepository) {
        return new GetBalanceUseCase(itemRepository, movementRepository);
    }
}
