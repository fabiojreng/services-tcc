package br.edu.ifma.labmanager.identity.application.use_cases;

import br.edu.ifma.labmanager.identity.domain.entities.User;
import br.edu.ifma.labmanager.identity.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.identity.domain.policy.AuthorizationPolicy;
import br.edu.ifma.labmanager.identity.domain.repository.UserRepository;
import br.edu.ifma.labmanager.identity.domain.value_objects.UserId;

import java.util.Objects;

public class CheckPermissionUseCase {

    private final UserRepository userRepository;

    public CheckPermissionUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public CheckPermissionResult execute(CheckPermissionCommand command) {
        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new DomainException("Usuário não encontrado: " + command.userId()));

        boolean allowed = AuthorizationPolicy.allows(user, command.permission());
        return new CheckPermissionResult(
                command.userId(),
                command.permission().name(),
                allowed
        );
    }
}
