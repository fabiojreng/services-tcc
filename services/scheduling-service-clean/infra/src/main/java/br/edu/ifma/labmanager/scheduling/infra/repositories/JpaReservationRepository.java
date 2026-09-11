package br.edu.ifma.labmanager.scheduling.infra.repositories;

import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.ReservationId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.TimeSlot;
import br.edu.ifma.labmanager.scheduling.infra.models.ReservationJpaEntity;

import java.util.List;
import java.util.Optional;

public class JpaReservationRepository implements ReservationRepository {

    private final SpringDataReservationRepository springData;

    public JpaReservationRepository(SpringDataReservationRepository springData) {
        this.springData = springData;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationJpaEntity entity = toEntity(reservation);
        springData.save(entity);
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

    private ReservationJpaEntity toEntity(Reservation reservation) {
        return new ReservationJpaEntity(
                reservation.id().value(),
                reservation.laboratoryId().value(),
                reservation.requesterId(),
                reservation.slot().start(),
                reservation.slot().end(),
                reservation.status()
        );
    }

    private Reservation toDomain(ReservationJpaEntity entity) {
        return Reservation.restore(
                ReservationId.of(entity.getId()),
                LaboratoryId.of(entity.getLaboratoryId()),
                entity.getRequesterId(),
                TimeSlot.of(entity.getStartAt(), entity.getEndAt()),
                entity.getStatus()
        );
    }
}
