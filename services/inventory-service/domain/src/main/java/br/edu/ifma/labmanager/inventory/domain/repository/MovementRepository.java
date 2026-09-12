package br.edu.ifma.labmanager.inventory.domain.repository;

import br.edu.ifma.labmanager.inventory.domain.entities.Movement;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;

import java.util.List;

public interface MovementRepository {
    Movement save(Movement movement);
    List<Movement> findByItemId(ItemId itemId);
}
