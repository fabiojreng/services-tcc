package br.edu.ifma.labmanager.catalog.application.use_cases;

import br.edu.ifma.labmanager.catalog.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.catalog.domain.entities.Laboratory;
import br.edu.ifma.labmanager.catalog.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.catalog.domain.repository.LaboratoryRepository;

import java.util.Objects;

public class RegisterLaboratoryUseCase {

    public static final String LABORATORY_MANAGE = "LABORATORY_MANAGE";

    private final LaboratoryRepository laboratoryRepository;
    private final IdentityGateway identityGateway;

    public RegisterLaboratoryUseCase(
            LaboratoryRepository laboratoryRepository,
            IdentityGateway identityGateway
    ) {
        this.laboratoryRepository = Objects.requireNonNull(laboratoryRepository);
        this.identityGateway = Objects.requireNonNull(identityGateway);
    }

    public Laboratory execute(RegisterLaboratoryCommand command) {
        if (!identityGateway.hasPermission(command.requesterId(), LABORATORY_MANAGE)) {
            throw new DomainException("Usuário sem permissão LABORATORY_MANAGE");
        }
        Laboratory laboratory = Laboratory.create(
                command.name(),
                command.capacity(),
                command.operatingHours()
        );
        return laboratoryRepository.save(laboratory);
    }
}
