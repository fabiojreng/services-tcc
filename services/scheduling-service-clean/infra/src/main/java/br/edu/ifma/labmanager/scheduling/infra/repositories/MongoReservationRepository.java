package br.edu.ifma.labmanager.scheduling.infra.repositories;

import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.ReservationId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.TimeSlot;
import br.edu.ifma.labmanager.scheduling.infra.models.ReservationDocument;

import java.util.List;
import java.util.Optional;

public class MongoReservationRepository implements ReservationRepository {

    private final SpringDataReservationRepository springData;

    public MongoReservationRepository(SpringDataReservationRepository springData) {
        this.springData = springData;
    }

    @Override
    public Reservation save(Reservation reservation) {
        springData.save(toDocument(reservation));
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return springData.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Reservation> findByLaboratoryId(LaboratoryId laboratoryId) {
        return springData.findByLaboratoryId(laboratoryId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    private ReservationDocument toDocument(Reservation reservation) {
        return new ReservationDocument(
                reservation.id().value(),
                reservation.laboratoryId().value(),
                reservation.requesterId(),
                reservation.slot().start(),
                reservation.slot().end(),
                reservation.status()
        );
    }

    private Reservation toDomain(ReservationDocument document) {
        return Reservation.restore(
                ReservationId.of(document.getId()),
                LaboratoryId.of(document.getLaboratoryId()),
                document.getRequesterId(),
                TimeSlot.of(document.getStartAt(), document.getEndAt()),
                document.getStatus()
        );
    }
}
