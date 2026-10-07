package br.edu.ifma.labmanager.scheduling.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "br.edu.ifma.labmanager.scheduling.infra",
        "br.edu.ifma.labmanager.scheduling.presentation"
})
public class SchedulingCleanApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingCleanApplication.class, args);
    }
}
