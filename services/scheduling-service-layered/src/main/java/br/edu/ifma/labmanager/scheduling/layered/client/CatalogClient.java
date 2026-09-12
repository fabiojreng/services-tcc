package br.edu.ifma.labmanager.scheduling.layered.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(
            RestClient.Builder builder,
            @Value("${labmanager.catalog.base-url:http://localhost:8082}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @SuppressWarnings("unchecked")
    public LaboratoryHours getLaboratory(UUID laboratoryId) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/api/laboratories/{id}", laboratoryId)
                    .retrieve()
                    .body(Map.class);
            if (body == null) {
                throw new IllegalStateException("Laboratório não encontrado: " + laboratoryId);
            }
            LocalTime opensAt = LocalTime.parse(String.valueOf(body.get("opensAt")));
            LocalTime closesAt = LocalTime.parse(String.valueOf(body.get("closesAt")));
            Set<DayOfWeek> openDays = EnumSet.noneOf(DayOfWeek.class);
            Object days = body.get("openDays");
            if (days instanceof List<?> list) {
                for (Object day : list) {
                    openDays.add(DayOfWeek.valueOf(String.valueOf(day)));
                }
            }
            return new LaboratoryHours(opensAt, closesAt, openDays);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Falha ao consultar catalog-service: " + ex.getMessage(), ex);
        }
    }

    public record LaboratoryHours(LocalTime opensAt, LocalTime closesAt, Set<DayOfWeek> openDays) {
    }
}
