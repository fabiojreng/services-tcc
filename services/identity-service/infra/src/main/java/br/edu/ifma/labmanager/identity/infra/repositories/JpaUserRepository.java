package br.edu.ifma.labmanager.identity.infra.repositories;

import br.edu.ifma.labmanager.identity.domain.entities.User;
import br.edu.ifma.labmanager.identity.domain.repository.UserRepository;
import br.edu.ifma.labmanager.identity.domain.value_objects.UserId;
import br.edu.ifma.labmanager.identity.infra.models.UserJpaEntity;

import java.util.Optional;

public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springData;

    public JpaUserRepository(SpringDataUserRepository springData) {
        this.springData = springData;
    }

    @Override
    public User save(User user) {
        springData.save(new UserJpaEntity(user.id().value(), user.name(), user.roles()));
        return user;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springData.findById(id.value()).map(this::toDomain);
    }

    @Override
    public boolean existsById(UserId id) {
        return springData.existsById(id.value());
    }

    private User toDomain(UserJpaEntity entity) {
        return User.restore(UserId.of(entity.getId()), entity.getName(), entity.getRoles());
    }
}
