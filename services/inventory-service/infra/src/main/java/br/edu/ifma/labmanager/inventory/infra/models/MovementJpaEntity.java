package br.edu.ifma.labmanager.inventory.infra.models;

import br.edu.ifma.labmanager.inventory.domain.value_objects.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movements")
public class MovementJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    protected MovementJpaEntity() {
    }

    public MovementJpaEntity(
            UUID id,
            UUID itemId,
            MovementType type,
            int quantity,
            String performedBy,
            LocalDateTime occurredAt
    ) {
        this.id = id;
        this.itemId = itemId;
        this.type = type;
        this.quantity = quantity;
        this.performedBy = performedBy;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getItemId() {
        return itemId;
    }

    public MovementType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
