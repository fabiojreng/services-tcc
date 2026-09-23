package br.edu.ifma.labmanager.scheduling.infra.repositories;

import br.edu.ifma.labmanager.scheduling.infra.models.ReservationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataReservationRepository extends MongoRepository<ReservationDocument, UUID> {

    List<ReservationDocument> findByLaboratoryId(UUID laboratoryId);
}
