package br.edu.ifma.labmanager.inventory.infra.repositories;

import br.edu.ifma.labmanager.inventory.domain.entities.Movement;
import br.edu.ifma.labmanager.inventory.domain.repository.MovementRepository;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;
import br.edu.ifma.labmanager.inventory.infra.models.MovementJpaEntity;

import java.util.List;

public class JpaMovementRepository implements MovementRepository {

    private final SpringDataMovementRepository springData;

    public JpaMovementRepository(SpringDataMovementRepository springData) {
        this.springData = springData;
    }

    @Override
    public Movement save(Movement movement) {
        springData.save(new MovementJpaEntity(
                movement.id(),
                movement.itemId().value(),
                movement.type(),
                movement.quantity(),
                movement.performedBy(),
                movement.occurredAt()
        ));
        return movement;
    }

    @Override
    public List<Movement> findByItemId(ItemId itemId) {
        return springData.findByItemId(itemId.value()).stream()
                .map(entity -> Movement.restore(
                        entity.getId(),
                        ItemId.of(entity.getItemId()),
                        entity.getType(),
                        entity.getQuantity(),
                        entity.getPerformedBy(),
                        entity.getOccurredAt()
                ))
                .toList();
    }
}
