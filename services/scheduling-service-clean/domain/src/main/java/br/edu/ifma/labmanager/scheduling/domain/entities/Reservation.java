package br.edu.ifma.labmanager.scheduling.domain.entities;

import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.ReservationId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.TimeSlot;

import java.util.Objects;

public final class Reservation {

    private final ReservationId id;
    private final LaboratoryId laboratoryId;
    private final String requesterId;
    private final TimeSlot slot;
    private ReservationStatus status;

    private Reservation(
            ReservationId id,
            LaboratoryId laboratoryId,
            String requesterId,
            TimeSlot slot,
            ReservationStatus status
    ) {
        this.id = Objects.requireNonNull(id);
        this.laboratoryId = Objects.requireNonNull(laboratoryId);
        this.requesterId = Objects.requireNonNull(requesterId);
        if (requesterId.isBlank()) {
            throw new IllegalArgumentException("requesterId obrigatório");
        }
        this.slot = Objects.requireNonNull(slot);
        this.status = Objects.requireNonNull(status);
    }

    public static Reservation request(
            LaboratoryId laboratoryId,
            String requesterId,
            TimeSlot slot
    ) {
        return new Reservation(
                ReservationId.generate(),
                laboratoryId,
                requesterId,
                slot,
                ReservationStatus.REQUESTED
        );
    }

    public static Reservation restore(
            ReservationId id,
            LaboratoryId laboratoryId,
            String requesterId,
            TimeSlot slot,
            ReservationStatus status
    ) {
        return new Reservation(id, laboratoryId, requesterId, slot, status);
    }

    public void confirm() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Não é possível confirmar uma reserva cancelada");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public ReservationId id() {
        return id;
    }

    public LaboratoryId laboratoryId() {
        return laboratoryId;
    }

    public String requesterId() {
        return requesterId;
    }

    public TimeSlot slot() {
        return slot;
    }

    public ReservationStatus status() {
        return status;
    }
}
