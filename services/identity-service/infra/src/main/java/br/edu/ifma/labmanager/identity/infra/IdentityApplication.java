package br.edu.ifma.labmanager.identity.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "br.edu.ifma.labmanager.identity.infra",
        "br.edu.ifma.labmanager.identity.presentation"
})
@EntityScan(basePackages = "br.edu.ifma.labmanager.identity.infra.models")
@EnableJpaRepositories(basePackages = "br.edu.ifma.labmanager.identity.infra.repositories")
public class IdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }
}
