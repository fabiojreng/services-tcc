package br.edu.ifma.labmanager.catalog.presentation.routes;

import br.edu.ifma.labmanager.catalog.application.use_cases.GetLaboratoryUseCase;
import br.edu.ifma.labmanager.catalog.application.use_cases.RegisterLaboratoryCommand;
import br.edu.ifma.labmanager.catalog.application.use_cases.RegisterLaboratoryUseCase;
import br.edu.ifma.labmanager.catalog.domain.entities.Laboratory;
import br.edu.ifma.labmanager.catalog.domain.value_objects.OperatingHours;
import br.edu.ifma.labmanager.catalog.presentation.schemas.LaboratoryResponse;
import br.edu.ifma.labmanager.catalog.presentation.schemas.RegisterLaboratoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/laboratories")
public class LaboratoryController {

    private final RegisterLaboratoryUseCase registerLaboratoryUseCase;
    private final GetLaboratoryUseCase getLaboratoryUseCase;

    public LaboratoryController(
            RegisterLaboratoryUseCase registerLaboratoryUseCase,
            GetLaboratoryUseCase getLaboratoryUseCase
    ) {
        this.registerLaboratoryUseCase = registerLaboratoryUseCase;
        this.getLaboratoryUseCase = getLaboratoryUseCase;
    }

    @PostMapping
    public ResponseEntity<LaboratoryResponse> register(@RequestBody RegisterLaboratoryRequest body) {
        Laboratory laboratory = registerLaboratoryUseCase.execute(new RegisterLaboratoryCommand(
                body.requesterId(),
                body.name(),
                body.capacity(),
                OperatingHours.of(body.opensAt(), body.closesAt(), body.openDays())
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(laboratory));
    }

    @GetMapping("/{id}")
    public LaboratoryResponse get(@PathVariable UUID id) {
        return toResponse(getLaboratoryUseCase.execute(id));
    }

    private LaboratoryResponse toResponse(Laboratory laboratory) {
        return new LaboratoryResponse(
                laboratory.id().value(),
                laboratory.name(),
                laboratory.capacity(),
                laboratory.operatingHours().opensAt(),
                laboratory.operatingHours().closesAt(),
                laboratory.operatingHours().openDays()
        );
    }
}
