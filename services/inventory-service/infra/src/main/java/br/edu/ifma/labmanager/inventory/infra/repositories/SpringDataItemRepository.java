package br.edu.ifma.labmanager.inventory.infra.repositories;

import br.edu.ifma.labmanager.inventory.infra.models.ItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataItemRepository extends JpaRepository<ItemJpaEntity, UUID> {
}
