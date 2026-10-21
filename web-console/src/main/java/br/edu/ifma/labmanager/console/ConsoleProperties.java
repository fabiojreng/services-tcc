package br.edu.ifma.labmanager.console;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "labmanager.console")
public record ConsoleProperties(
        Map<String, String> services,
        Duration connectTimeout,
        Duration requestTimeout
) {

    public ConsoleProperties {
        services = services == null ? Map.of() : new LinkedHashMap<>(services);
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        requestTimeout = requestTimeout == null ? Duration.ofSeconds(10) : requestTimeout;
    }
}
