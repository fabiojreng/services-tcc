package br.edu.ifma.labmanager.catalog.infra.repositories;

import br.edu.ifma.labmanager.catalog.infra.models.LaboratoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataLaboratoryRepository extends JpaRepository<LaboratoryJpaEntity, UUID> {
}
