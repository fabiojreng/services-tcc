package br.edu.ifma.labmanager.console;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class ServiceProxyController {

    private static final List<String> FORWARDED_HEADERS = List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT);

    private final ConsoleProperties properties;
    private final HttpClient httpClient;

    public ServiceProxyController(ConsoleProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
    }

    @GetMapping("/console/services")
    public List<ServiceStatus> services() {
        List<CompletableFuture<ServiceStatus>> probes = properties.services().entrySet().stream()
                .map(entry -> probe(entry.getKey(), entry.getValue()))
                .toList();
        return probes.stream().map(CompletableFuture::join).toList();
    }

    @RequestMapping("/proxy/{service}/**")
    public ResponseEntity<?> forward(
            @PathVariable String service,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request
    ) {
        String baseUrl = properties.services().get(service);
        if (baseUrl == null) {
            return error(HttpStatus.NOT_FOUND, "Serviço desconhecido: " + service);
        }

        String prefix = request.getContextPath() + "/proxy/" + service;
        String path = request.getRequestURI().substring(prefix.length());
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();

        HttpRequest.Builder upstream = HttpRequest.newBuilder(URI.create(baseUrl + path + query))
                .timeout(properties.requestTimeout())
                .method(request.getMethod(), body == null || body.length == 0
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofByteArray(body));
        for (String header : FORWARDED_HEADERS) {
            String value = request.getHeader(header);
            if (value != null) {
                upstream.header(header, value);
            }
        }

        try {
            HttpResponse<byte[]> response = httpClient.send(upstream.build(), HttpResponse.BodyHandlers.ofByteArray());
            ResponseEntity.BodyBuilder result = ResponseEntity.status(response.statusCode());
            response.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                    .ifPresent(type -> result.contentType(MediaType.parseMediaType(type)));
            return result.body(response.body());
        } catch (HttpTimeoutException ex) {
            return error(HttpStatus.GATEWAY_TIMEOUT, "Tempo esgotado aguardando " + service + " (" + baseUrl + ")");
        } catch (ConnectException ex) {
            return error(HttpStatus.BAD_GATEWAY, "Serviço " + service + " indisponível em " + baseUrl);
        } catch (IOException ex) {
            return error(HttpStatus.BAD_GATEWAY, "Falha ao contatar " + service + ": " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return error(HttpStatus.SERVICE_UNAVAILABLE, "Requisição interrompida");
        }
    }

    private CompletableFuture<ServiceStatus> probe(String name, String baseUrl) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/"))
                .timeout(properties.connectTimeout())
                .GET()
                .build();
        long startedAt = System.nanoTime();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .handle((response, failure) -> new ServiceStatus(
                        name,
                        baseUrl,
                        failure == null,
                        failure == null ? (System.nanoTime() - startedAt) / 1_000_000 : null
                ));
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    public record ServiceStatus(String name, String baseUrl, boolean up, Long latencyMs) {
    }
}
