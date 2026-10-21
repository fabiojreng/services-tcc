package br.edu.ifma.labmanager.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebConsoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebConsoleApplication.class, args);
    }
}
