package br.edu.ifma.labmanager.identity.domain.repository;

import br.edu.ifma.labmanager.identity.domain.entities.User;
import br.edu.ifma.labmanager.identity.domain.value_objects.UserId;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    boolean existsById(UserId id);
}
