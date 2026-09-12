package br.edu.ifma.labmanager.inventory.application.use_cases;

import br.edu.ifma.labmanager.inventory.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.inventory.domain.entities.Item;
import br.edu.ifma.labmanager.inventory.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.inventory.domain.repository.ItemRepository;

import java.util.Objects;

public class RegisterItemUseCase {

    public static final String INVENTORY_MANAGE = "INVENTORY_MANAGE";

    private final ItemRepository itemRepository;
    private final IdentityGateway identityGateway;

    public RegisterItemUseCase(ItemRepository itemRepository, IdentityGateway identityGateway) {
        this.itemRepository = Objects.requireNonNull(itemRepository);
        this.identityGateway = Objects.requireNonNull(identityGateway);
    }

    public Item execute(RegisterItemCommand command) {
        if (!identityGateway.hasPermission(command.requesterId(), INVENTORY_MANAGE)) {
            throw new DomainException("Usuário sem permissão INVENTORY_MANAGE");
        }
        Item item = Item.create(command.name(), command.unit());
        return itemRepository.save(item);
    }
}
