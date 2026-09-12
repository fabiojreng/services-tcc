package br.edu.ifma.labmanager.inventory.infra.repositories;

import br.edu.ifma.labmanager.inventory.infra.models.MovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataMovementRepository extends JpaRepository<MovementJpaEntity, UUID> {

    List<MovementJpaEntity> findByItemId(UUID itemId);
}
