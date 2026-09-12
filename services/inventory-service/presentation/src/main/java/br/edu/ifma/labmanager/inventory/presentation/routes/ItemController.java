package br.edu.ifma.labmanager.inventory.presentation.routes;

import br.edu.ifma.labmanager.inventory.application.use_cases.GetBalanceUseCase;
import br.edu.ifma.labmanager.inventory.application.use_cases.RegisterItemCommand;
import br.edu.ifma.labmanager.inventory.application.use_cases.RegisterItemUseCase;
import br.edu.ifma.labmanager.inventory.application.use_cases.RegisterMovementCommand;
import br.edu.ifma.labmanager.inventory.application.use_cases.RegisterMovementUseCase;
import br.edu.ifma.labmanager.inventory.domain.entities.Item;
import br.edu.ifma.labmanager.inventory.domain.entities.Movement;
import br.edu.ifma.labmanager.inventory.presentation.schemas.BalanceResponse;
import br.edu.ifma.labmanager.inventory.presentation.schemas.ItemResponse;
import br.edu.ifma.labmanager.inventory.presentation.schemas.MovementResponse;
import br.edu.ifma.labmanager.inventory.presentation.schemas.RegisterItemRequest;
import br.edu.ifma.labmanager.inventory.presentation.schemas.RegisterMovementRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final RegisterItemUseCase registerItemUseCase;
    private final RegisterMovementUseCase registerMovementUseCase;
    private final GetBalanceUseCase getBalanceUseCase;

    public ItemController(
            RegisterItemUseCase registerItemUseCase,
            RegisterMovementUseCase registerMovementUseCase,
            GetBalanceUseCase getBalanceUseCase
    ) {
        this.registerItemUseCase = registerItemUseCase;
        this.registerMovementUseCase = registerMovementUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> register(@RequestBody RegisterItemRequest body) {
        Item item = registerItemUseCase.execute(new RegisterItemCommand(
                body.requesterId(),
                body.name(),
                body.unit()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toItemResponse(item));
    }

    @PostMapping("/{id}/movements")
    public ResponseEntity<MovementResponse> registerMovement(
            @PathVariable UUID id,
            @RequestBody RegisterMovementRequest body
    ) {
        Movement movement = registerMovementUseCase.execute(new RegisterMovementCommand(
                body.requesterId(),
                id,
                body.type(),
                body.quantity()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toMovementResponse(movement));
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse balance(@PathVariable UUID id) {
        return new BalanceResponse(id, getBalanceUseCase.execute(id));
    }

    private ItemResponse toItemResponse(Item item) {
        return new ItemResponse(item.id().value(), item.name(), item.unit());
    }

    private MovementResponse toMovementResponse(Movement movement) {
        return new MovementResponse(
                movement.id(),
                movement.itemId().value(),
                movement.type(),
                movement.quantity(),
                movement.performedBy(),
                movement.occurredAt()
        );
    }
}
