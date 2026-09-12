package br.edu.ifma.labmanager.inventory.application.use_cases;

import br.edu.ifma.labmanager.inventory.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.inventory.domain.policy.BalancePolicy;
import br.edu.ifma.labmanager.inventory.domain.repository.ItemRepository;
import br.edu.ifma.labmanager.inventory.domain.repository.MovementRepository;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;

import java.util.Objects;
import java.util.UUID;

public class GetBalanceUseCase {

    private final ItemRepository itemRepository;
    private final MovementRepository movementRepository;

    public GetBalanceUseCase(ItemRepository itemRepository, MovementRepository movementRepository) {
        this.itemRepository = Objects.requireNonNull(itemRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
    }

    public int execute(UUID itemId) {
        ItemId id = ItemId.of(itemId);
        itemRepository.findById(id)
                .orElseThrow(() -> new DomainException("Item não encontrado: " + itemId));
        return BalancePolicy.calculate(movementRepository.findByItemId(id));
    }
}
