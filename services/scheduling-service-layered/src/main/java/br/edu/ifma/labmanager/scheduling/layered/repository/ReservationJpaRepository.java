package br.edu.ifma.labmanager.scheduling.layered.repository;

import br.edu.ifma.labmanager.scheduling.layered.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, UUID> {

    List<ReservationEntity> findByLaboratoryId(UUID laboratoryId);
}
