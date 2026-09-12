package br.edu.ifma.labmanager.inventory.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "br.edu.ifma.labmanager.inventory.infra",
        "br.edu.ifma.labmanager.inventory.presentation"
})
@EntityScan(basePackages = "br.edu.ifma.labmanager.inventory.infra.models")
@EnableJpaRepositories(basePackages = "br.edu.ifma.labmanager.inventory.infra.repositories")
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
