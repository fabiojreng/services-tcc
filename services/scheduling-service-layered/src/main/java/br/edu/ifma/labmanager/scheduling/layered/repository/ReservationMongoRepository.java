package br.edu.ifma.labmanager.scheduling.layered.repository;

import br.edu.ifma.labmanager.scheduling.layered.entity.ReservationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationMongoRepository extends MongoRepository<ReservationEntity, UUID> {

    List<ReservationEntity> findByLaboratoryId(UUID laboratoryId);
}
