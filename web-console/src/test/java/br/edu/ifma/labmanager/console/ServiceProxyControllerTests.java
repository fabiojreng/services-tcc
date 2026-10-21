package br.edu.ifma.labmanager.console;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceProxyControllerTests {

    private static final HttpServer UPSTREAM = startUpstream();

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void services(DynamicPropertyRegistry registry) {
        registry.add("labmanager.console.services.identity", () -> "http://localhost:" + UPSTREAM.getAddress().getPort());
        registry.add("labmanager.console.services.catalog", () -> "http://localhost:1");
    }

    @AfterAll
    static void stopUpstream() {
        UPSTREAM.stop(0);
    }

    @Test
    void encaminhaMetodoCaminhoECorpoEPreservaStatusDoServico() throws Exception {
        mockMvc.perform(post("/proxy/identity/api/users?origem=console")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"aluno-1\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.path").value("/api/users"))
                .andExpect(jsonPath("$.query").value("origem=console"))
                .andExpect(jsonPath("$.body").value("{\"userId\":\"aluno-1\"}"));
    }

    @Test
    void servicoDesconhecidoRetorna404() throws Exception {
        mockMvc.perform(get("/proxy/inexistente/api/x"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void servicoForaDoArRetorna502() throws Exception {
        mockMvc.perform(get("/proxy/catalog/api/laboratories/123"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void statusIndicaServicosOnlineEOffline() throws Exception {
        mockMvc.perform(get("/console/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'identity')].up").value(true))
                .andExpect(jsonPath("$[?(@.name == 'catalog')].up").value(false));
    }

    @Test
    void serveAInterfaceEstatica() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("LabManager")));
    }

    private static HttpServer startUpstream() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String json = """
                        {"method":"%s","path":"%s","query":"%s","body":%s}
                        """.formatted(
                        exchange.getRequestMethod(),
                        exchange.getRequestURI().getPath(),
                        exchange.getRequestURI().getQuery(),
                        body.isEmpty() ? "null" : "\"" + body.replace("\"", "\\\"") + "\""
                );
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(422, bytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
