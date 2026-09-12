package br.edu.ifma.labmanager.inventory.application.use_cases;

import br.edu.ifma.labmanager.inventory.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.inventory.domain.entities.Movement;
import br.edu.ifma.labmanager.inventory.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.inventory.domain.policy.BalancePolicy;
import br.edu.ifma.labmanager.inventory.domain.repository.ItemRepository;
import br.edu.ifma.labmanager.inventory.domain.repository.MovementRepository;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;
import br.edu.ifma.labmanager.inventory.domain.value_objects.MovementType;

import java.time.LocalDateTime;
import java.util.Objects;

public class RegisterMovementUseCase {

    public static final String INVENTORY_MANAGE = "INVENTORY_MANAGE";

    private final ItemRepository itemRepository;
    private final MovementRepository movementRepository;
    private final IdentityGateway identityGateway;

    public RegisterMovementUseCase(
            ItemRepository itemRepository,
            MovementRepository movementRepository,
            IdentityGateway identityGateway
    ) {
        this.itemRepository = Objects.requireNonNull(itemRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
        this.identityGateway = Objects.requireNonNull(identityGateway);
    }

    public Movement execute(RegisterMovementCommand command) {
        if (!identityGateway.hasPermission(command.requesterId(), INVENTORY_MANAGE)) {
            throw new DomainException("Usuário sem permissão INVENTORY_MANAGE");
        }

        ItemId itemId = ItemId.of(command.itemId());
        itemRepository.findById(itemId)
                .orElseThrow(() -> new DomainException("Item não encontrado: " + command.itemId()));

        if (command.type() == MovementType.OUT) {
            int balance = BalancePolicy.calculate(movementRepository.findByItemId(itemId));
            BalancePolicy.ensureSufficientBalance(balance, command.quantity());
        }

        Movement movement = Movement.create(
                itemId,
                command.type(),
                command.quantity(),
                command.requesterId(),
                LocalDateTime.now()
        );
        return movementRepository.save(movement);
    }
}
