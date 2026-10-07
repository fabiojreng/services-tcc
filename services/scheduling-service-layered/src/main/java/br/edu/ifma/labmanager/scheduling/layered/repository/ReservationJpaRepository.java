package br.edu.ifma.labmanager.scheduling.layered.repository;

import br.edu.ifma.labmanager.scheduling.layered.entity.ReservationEntity;
import br.edu.ifma.labmanager.scheduling.layered.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, UUID> {

    List<ReservationEntity> findByLaboratoryId(UUID laboratoryId);

    long countByRequesterIdAndStatusInAndStartAtGreaterThanEqualAndStartAtLessThan(
            String requesterId, Collection<ReservationStatus> statuses, LocalDateTime from, LocalDateTime toExclusive);
}
