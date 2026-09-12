package br.edu.ifma.labmanager.scheduling.infra.http;

import br.edu.ifma.labmanager.scheduling.application.RemoteDependencyException;
import br.edu.ifma.labmanager.scheduling.application.ports.IdentityGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class HttpIdentityGateway implements IdentityGateway {

    private final RestClient restClient;

    public HttpIdentityGateway(
            RestClient.Builder builder,
            @Value("${labmanager.identity.base-url:http://localhost:8080}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasPermission(String userId, String permission) {
        try {
            Map<String, Object> body = restClient.post()
                    .uri("/api/permissions/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("userId", userId, "permission", permission))
                    .retrieve()
                    .body(Map.class);

            if (body == null || !body.containsKey("allowed")) {
                throw new RemoteDependencyException("Resposta inválida do identity-service");
            }
            return Boolean.TRUE.equals(body.get("allowed"));
        } catch (RestClientException ex) {
            throw new RemoteDependencyException("Falha ao consultar identity-service: " + ex.getMessage(), ex);
        }
    }
}
