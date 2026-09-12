package br.edu.ifma.labmanager.scheduling.infra.http;

import br.edu.ifma.labmanager.scheduling.application.RemoteDependencyException;
import br.edu.ifma.labmanager.scheduling.application.ports.CatalogGateway;
import br.edu.ifma.labmanager.scheduling.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class HttpCatalogGateway implements CatalogGateway {

    private final RestClient restClient;

    public HttpCatalogGateway(
            RestClient.Builder builder,
            @Value("${labmanager.catalog.base-url:http://localhost:8082}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public OperatingHours getOperatingHours(LaboratoryId laboratoryId) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/api/laboratories/{id}", laboratoryId.value())
                    .retrieve()
                    .body(Map.class);

            if (body == null) {
                throw new DomainException("Laboratório não encontrado: " + laboratoryId);
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
            return OperatingHours.of(opensAt, closesAt, openDays);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 422 || ex.getStatusCode().value() == 404) {
                throw new DomainException("Laboratório não encontrado: " + laboratoryId);
            }
            throw new RemoteDependencyException("Falha ao consultar catalog-service", ex);
        } catch (RestClientException ex) {
            throw new RemoteDependencyException("Falha ao consultar catalog-service: " + ex.getMessage(), ex);
        }
    }
}
