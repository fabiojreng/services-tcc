package br.edu.ifma.labmanager.catalog.domain.repository;

import br.edu.ifma.labmanager.catalog.domain.entities.Laboratory;
import br.edu.ifma.labmanager.catalog.domain.value_objects.LaboratoryId;

import java.util.Optional;

public interface LaboratoryRepository {
    Laboratory save(Laboratory laboratory);
    Optional<Laboratory> findById(LaboratoryId id);
}
