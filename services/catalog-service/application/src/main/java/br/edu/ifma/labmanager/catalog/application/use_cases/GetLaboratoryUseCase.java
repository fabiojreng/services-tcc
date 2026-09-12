package br.edu.ifma.labmanager.catalog.application.use_cases;

import br.edu.ifma.labmanager.catalog.domain.entities.Laboratory;
import br.edu.ifma.labmanager.catalog.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.catalog.domain.repository.LaboratoryRepository;
import br.edu.ifma.labmanager.catalog.domain.value_objects.LaboratoryId;

import java.util.Objects;
import java.util.UUID;

public class GetLaboratoryUseCase {

    private final LaboratoryRepository laboratoryRepository;

    public GetLaboratoryUseCase(LaboratoryRepository laboratoryRepository) {
        this.laboratoryRepository = Objects.requireNonNull(laboratoryRepository);
    }

    public Laboratory execute(UUID laboratoryId) {
        return laboratoryRepository.findById(LaboratoryId.of(laboratoryId))
                .orElseThrow(() -> new DomainException("Laboratório não encontrado: " + laboratoryId));
    }
}
