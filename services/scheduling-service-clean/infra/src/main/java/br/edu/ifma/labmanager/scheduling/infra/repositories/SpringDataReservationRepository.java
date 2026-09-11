package br.edu.ifma.labmanager.scheduling.infra.repositories;

import br.edu.ifma.labmanager.scheduling.infra.models.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataReservationRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    List<ReservationJpaEntity> findByLaboratoryId(UUID laboratoryId);
}
