package br.edu.ifma.labmanager.scheduling.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "br.edu.ifma.labmanager.scheduling.infrastructure",
        "br.edu.ifma.labmanager.scheduling.adapter"
})
@EntityScan(basePackages = "br.edu.ifma.labmanager.scheduling.adapter.persistence")
@EnableJpaRepositories(basePackages = "br.edu.ifma.labmanager.scheduling.adapter.persistence")
public class SchedulingCleanApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingCleanApplication.class, args);
    }
}
