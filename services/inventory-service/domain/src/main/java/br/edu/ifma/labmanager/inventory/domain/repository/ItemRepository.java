package br.edu.ifma.labmanager.inventory.domain.repository;

import br.edu.ifma.labmanager.inventory.domain.entities.Item;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;

import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);
    Optional<Item> findById(ItemId id);
}
