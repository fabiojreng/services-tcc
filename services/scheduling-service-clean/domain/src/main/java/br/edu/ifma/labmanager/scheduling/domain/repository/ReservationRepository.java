package br.edu.ifma.labmanager.scheduling.domain.repository;

import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.ReservationId;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(ReservationId id);

    List<Reservation> findByLaboratoryId(LaboratoryId laboratoryId);
}
