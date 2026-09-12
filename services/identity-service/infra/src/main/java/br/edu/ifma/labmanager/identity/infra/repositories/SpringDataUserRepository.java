package br.edu.ifma.labmanager.identity.infra.repositories;

import br.edu.ifma.labmanager.identity.infra.models.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {
}
