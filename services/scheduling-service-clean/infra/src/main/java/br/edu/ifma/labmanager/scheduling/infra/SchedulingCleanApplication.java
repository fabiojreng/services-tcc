package br.edu.ifma.labmanager.scheduling.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "br.edu.ifma.labmanager.scheduling.infra",
        "br.edu.ifma.labmanager.scheduling.presentation"
})
@EntityScan(basePackages = "br.edu.ifma.labmanager.scheduling.infra.models")
@EnableJpaRepositories(basePackages = "br.edu.ifma.labmanager.scheduling.infra.repositories")
public class SchedulingCleanApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingCleanApplication.class, args);
    }
}
