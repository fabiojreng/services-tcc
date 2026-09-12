package br.edu.ifma.labmanager.catalog.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "br.edu.ifma.labmanager.catalog.infra",
        "br.edu.ifma.labmanager.catalog.presentation"
})
@EntityScan(basePackages = "br.edu.ifma.labmanager.catalog.infra.models")
@EnableJpaRepositories(basePackages = "br.edu.ifma.labmanager.catalog.infra.repositories")
public class CatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
