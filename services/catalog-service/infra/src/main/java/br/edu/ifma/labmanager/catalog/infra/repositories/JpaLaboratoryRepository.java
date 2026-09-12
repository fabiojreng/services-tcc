package br.edu.ifma.labmanager.catalog.infra.repositories;

import br.edu.ifma.labmanager.catalog.domain.entities.Laboratory;
import br.edu.ifma.labmanager.catalog.domain.repository.LaboratoryRepository;
import br.edu.ifma.labmanager.catalog.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.catalog.domain.value_objects.OperatingHours;
import br.edu.ifma.labmanager.catalog.infra.models.LaboratoryJpaEntity;

import java.util.Optional;

public class JpaLaboratoryRepository implements LaboratoryRepository {

    private final SpringDataLaboratoryRepository springData;

    public JpaLaboratoryRepository(SpringDataLaboratoryRepository springData) {
        this.springData = springData;
    }

    @Override
    public Laboratory save(Laboratory laboratory) {
        springData.save(new LaboratoryJpaEntity(
                laboratory.id().value(),
                laboratory.name(),
                laboratory.capacity(),
                laboratory.operatingHours().opensAt(),
                laboratory.operatingHours().closesAt(),
                laboratory.operatingHours().openDays()
        ));
        return laboratory;
    }

    @Override
    public Optional<Laboratory> findById(LaboratoryId id) {
        return springData.findById(id.value()).map(this::toDomain);
    }

    private Laboratory toDomain(LaboratoryJpaEntity entity) {
        return Laboratory.restore(
                LaboratoryId.of(entity.getId()),
                entity.getName(),
                entity.getCapacity(),
                OperatingHours.of(entity.getOpensAt(), entity.getClosesAt(), entity.getOpenDays())
        );
    }
}
