package br.edu.ifma.labmanager.identity.presentation.routes;

import br.edu.ifma.labmanager.identity.application.use_cases.CheckPermissionCommand;
import br.edu.ifma.labmanager.identity.application.use_cases.CheckPermissionResult;
import br.edu.ifma.labmanager.identity.application.use_cases.CheckPermissionUseCase;
import br.edu.ifma.labmanager.identity.application.use_cases.RegisterUserCommand;
import br.edu.ifma.labmanager.identity.application.use_cases.RegisterUserUseCase;
import br.edu.ifma.labmanager.identity.domain.entities.User;
import br.edu.ifma.labmanager.identity.presentation.schemas.CheckPermissionRequest;
import br.edu.ifma.labmanager.identity.presentation.schemas.CheckPermissionResponse;
import br.edu.ifma.labmanager.identity.presentation.schemas.RegisterUserRequest;
import br.edu.ifma.labmanager.identity.presentation.schemas.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IdentityController {

    private final RegisterUserUseCase registerUserUseCase;
    private final CheckPermissionUseCase checkPermissionUseCase;

    public IdentityController(
            RegisterUserUseCase registerUserUseCase,
            CheckPermissionUseCase checkPermissionUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.checkPermissionUseCase = checkPermissionUseCase;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterUserRequest body) {
        User user = registerUserUseCase.execute(
                new RegisterUserCommand(body.userId(), body.name(), body.roles())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(user.id().value(), user.name(), user.roles()));
    }

    @PostMapping("/permissions/check")
    public CheckPermissionResponse check(@RequestBody CheckPermissionRequest body) {
        CheckPermissionResult result = checkPermissionUseCase.execute(
                new CheckPermissionCommand(body.userId(), body.permission())
        );
        return new CheckPermissionResponse(result.userId(), result.permission(), result.allowed());
    }
}
