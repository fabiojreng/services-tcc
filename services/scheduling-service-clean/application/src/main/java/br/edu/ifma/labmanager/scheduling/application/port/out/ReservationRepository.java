package br.edu.ifma.labmanager.scheduling.application.port.out;

import br.edu.ifma.labmanager.scheduling.domain.model.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.model.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.model.ReservationId;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(ReservationId id);

    List<Reservation> findByLaboratoryId(LaboratoryId laboratoryId);
}
