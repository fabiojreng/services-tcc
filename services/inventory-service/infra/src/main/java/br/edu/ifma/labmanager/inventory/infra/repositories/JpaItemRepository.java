package br.edu.ifma.labmanager.inventory.infra.repositories;

import br.edu.ifma.labmanager.inventory.domain.entities.Item;
import br.edu.ifma.labmanager.inventory.domain.repository.ItemRepository;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;
import br.edu.ifma.labmanager.inventory.infra.models.ItemJpaEntity;

import java.util.Optional;

public class JpaItemRepository implements ItemRepository {

    private final SpringDataItemRepository springData;

    public JpaItemRepository(SpringDataItemRepository springData) {
        this.springData = springData;
    }

    @Override
    public Item save(Item item) {
        springData.save(new ItemJpaEntity(item.id().value(), item.name(), item.unit()));
        return item;
    }

    @Override
    public Optional<Item> findById(ItemId id) {
        return springData.findById(id.value()).map(entity ->
                Item.restore(ItemId.of(entity.getId()), entity.getName(), entity.getUnit())
        );
    }
}
