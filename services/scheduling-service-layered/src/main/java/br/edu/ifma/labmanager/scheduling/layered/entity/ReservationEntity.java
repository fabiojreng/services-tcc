package br.edu.ifma.labmanager.scheduling.layered.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID laboratoryId;

    @Column(nullable = false)
    private String requesterId;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    protected ReservationEntity() {
    }

    public ReservationEntity(
            UUID id,
            UUID laboratoryId,
            String requesterId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            ReservationStatus status
    ) {
        this.id = id;
        this.laboratoryId = laboratoryId;
        this.requesterId = requesterId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLaboratoryId() {
        return laboratoryId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
