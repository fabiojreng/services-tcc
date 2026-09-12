package br.edu.ifma.labmanager.inventory.infra.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "items")
public class ItemJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unit;

    protected ItemJpaEntity() {
    }

    public ItemJpaEntity(UUID id, String name, String unit) {
        this.id = id;
        this.name = name;
        this.unit = unit;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }
}
