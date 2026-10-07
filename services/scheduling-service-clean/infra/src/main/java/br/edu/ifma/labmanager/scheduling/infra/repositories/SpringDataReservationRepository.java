package br.edu.ifma.labmanager.scheduling.infra.repositories;

import br.edu.ifma.labmanager.scheduling.infra.models.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataReservationRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    List<ReservationJpaEntity> findByLaboratoryId(UUID laboratoryId);

    List<ReservationJpaEntity> findByRequesterIdAndStartAtGreaterThanEqualAndStartAtLessThan(
            String requesterId, LocalDateTime from, LocalDateTime toExclusive);
}
