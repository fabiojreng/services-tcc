package br.edu.ifma.labmanager.inventory.domain.entities;

import br.edu.ifma.labmanager.inventory.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;

import java.util.Objects;

public final class Item {

    private final ItemId id;
    private final String name;
    private final String unit;

    private Item(ItemId id, String name, String unit) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.unit = Objects.requireNonNull(unit);
        if (name.isBlank()) {
            throw new DomainException("Nome do item é obrigatório");
        }
        if (unit.isBlank()) {
            throw new DomainException("Unidade do item é obrigatória");
        }
    }

    public static Item create(String name, String unit) {
        return new Item(ItemId.generate(), name, unit);
    }

    public static Item restore(ItemId id, String name, String unit) {
        return new Item(id, name, unit);
    }

    public ItemId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String unit() {
        return unit;
    }
}
