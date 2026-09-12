package br.edu.ifma.labmanager.inventory.domain.policy;

import br.edu.ifma.labmanager.inventory.domain.entities.Movement;
import br.edu.ifma.labmanager.inventory.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;
import br.edu.ifma.labmanager.inventory.domain.value_objects.MovementType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BalancePolicyTest {

    private static final ItemId ITEM_ID = ItemId.of(UUID.randomUUID());

    @Test
    void calculaSaldoComEntradasESaidas() {
        List<Movement> movements = List.of(
                movement(MovementType.IN, 10),
                movement(MovementType.OUT, 3),
                movement(MovementType.IN, 5)
        );
        assertEquals(12, BalancePolicy.calculate(movements));
    }

    @Test
    void rejeitaSaidaMaiorQueSaldo() {
        assertThrows(DomainException.class, () -> BalancePolicy.ensureSufficientBalance(2, 5));
    }

    private Movement movement(MovementType type, int quantity) {
        return Movement.create(ITEM_ID, type, quantity, "tec-1", LocalDateTime.now());
    }
}
