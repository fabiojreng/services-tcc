package br.edu.ifma.labmanager.scheduling.application.ports;

import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;

public interface CatalogGateway {
    OperatingHours getOperatingHours(LaboratoryId laboratoryId);
}
