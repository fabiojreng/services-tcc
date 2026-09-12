package br.edu.ifma.labmanager.identity.application.use_cases;

import br.edu.ifma.labmanager.identity.domain.entities.User;
import br.edu.ifma.labmanager.identity.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.identity.domain.repository.UserRepository;
import br.edu.ifma.labmanager.identity.domain.value_objects.UserId;

import java.util.Objects;

public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    public User execute(RegisterUserCommand command) {
        UserId id = UserId.of(command.userId());
        if (userRepository.existsById(id)) {
            throw new DomainException("Usuário já existe: " + id.value());
        }
        User user = User.register(id, command.name(), command.roles());
        return userRepository.save(user);
    }
}
