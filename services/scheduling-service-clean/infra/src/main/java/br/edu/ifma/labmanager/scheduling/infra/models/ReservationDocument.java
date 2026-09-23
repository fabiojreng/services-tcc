package br.edu.ifma.labmanager.scheduling.infra.models;

import br.edu.ifma.labmanager.scheduling.domain.entities.ReservationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "reservations")
public class ReservationDocument {

    @Id
    private UUID id;
    private UUID laboratoryId;
    private String requesterId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;

    protected ReservationDocument() {
    }

    public ReservationDocument(
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
