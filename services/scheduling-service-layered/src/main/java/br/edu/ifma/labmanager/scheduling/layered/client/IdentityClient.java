package br.edu.ifma.labmanager.scheduling.layered.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class IdentityClient {

    private final RestClient restClient;

    public IdentityClient(
            RestClient.Builder builder,
            @Value("${labmanager.identity.base-url:http://localhost:8080}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @SuppressWarnings("unchecked")
    public boolean hasPermission(String userId, String permission) {
        try {
            Map<String, Object> body = restClient.post()
                    .uri("/api/permissions/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("userId", userId, "permission", permission))
                    .retrieve()
                    .body(Map.class);
            return body != null && Boolean.TRUE.equals(body.get("allowed"));
        } catch (RestClientException ex) {
            throw new IllegalStateException("Falha ao consultar identity-service: " + ex.getMessage(), ex);
        }
    }
}
