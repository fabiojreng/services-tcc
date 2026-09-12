package br.edu.ifma.labmanager.identity.infra.config;

import br.edu.ifma.labmanager.identity.application.use_cases.CheckPermissionUseCase;
import br.edu.ifma.labmanager.identity.application.use_cases.RegisterUserUseCase;
import br.edu.ifma.labmanager.identity.domain.repository.UserRepository;
import br.edu.ifma.labmanager.identity.infra.repositories.JpaUserRepository;
import br.edu.ifma.labmanager.identity.infra.repositories.SpringDataUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityConfig {

    @Bean
    UserRepository userRepository(SpringDataUserRepository springData) {
        return new JpaUserRepository(springData);
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepository userRepository) {
        return new RegisterUserUseCase(userRepository);
    }

    @Bean
    CheckPermissionUseCase checkPermissionUseCase(UserRepository userRepository) {
        return new CheckPermissionUseCase(userRepository);
    }
}
