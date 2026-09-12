package br.edu.ifma.labmanager.catalog.domain.entities;

import br.edu.ifma.labmanager.catalog.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.catalog.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.catalog.domain.value_objects.OperatingHours;

import java.util.Objects;

public final class Laboratory {

    private final LaboratoryId id;
    private final String name;
    private final int capacity;
    private final OperatingHours operatingHours;

    private Laboratory(LaboratoryId id, String name, int capacity, OperatingHours operatingHours) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        if (name.isBlank()) {
            throw new DomainException("Nome do laboratório é obrigatório");
        }
        if (capacity <= 0) {
            throw new DomainException("Capacidade deve ser positiva");
        }
        this.capacity = capacity;
        this.operatingHours = Objects.requireNonNull(operatingHours);
    }

    public static Laboratory create(String name, int capacity, OperatingHours operatingHours) {
        return new Laboratory(LaboratoryId.generate(), name, capacity, operatingHours);
    }

    public static Laboratory restore(
            LaboratoryId id,
            String name,
            int capacity,
            OperatingHours operatingHours
    ) {
        return new Laboratory(id, name, capacity, operatingHours);
    }

    public LaboratoryId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int capacity() {
        return capacity;
    }

    public OperatingHours operatingHours() {
        return operatingHours;
    }
}
