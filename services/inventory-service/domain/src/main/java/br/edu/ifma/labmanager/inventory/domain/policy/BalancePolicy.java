package br.edu.ifma.labmanager.inventory.domain.policy;

import br.edu.ifma.labmanager.inventory.domain.entities.Movement;
import br.edu.ifma.labmanager.inventory.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.inventory.domain.value_objects.MovementType;

import java.util.List;

public final class BalancePolicy {

    private BalancePolicy() {
    }

    public static int calculate(List<Movement> movements) {
        return movements.stream()
                .mapToInt(m -> m.type() == MovementType.IN ? m.quantity() : -m.quantity())
                .sum();
    }

    public static void ensureSufficientBalance(int currentBalance, int outQuantity) {
        if (outQuantity > currentBalance) {
            throw new DomainException("Saldo insuficiente para saída");
        }
    }
}
