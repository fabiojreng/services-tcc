package br.edu.ifma.labmanager.inventory.infra;

import br.edu.ifma.labmanager.inventory.application.ports.IdentityGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class Stubs {
        @Bean
        @Primary
        IdentityGateway identityGatewayStub() {
            return (userId, permission) -> true;
        }
    }

    @Test
    void registraItemMovimentaEConsultaSaldo() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requesterId": "tec-1",
                                  "name": "Álcool 70%",
                                  "unit": "ml"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Álcool 70%"))
                .andReturn();

        String json = created.getResponse().getContentAsString();
        String id = json.replaceAll("(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/items/" + id + "/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requesterId": "tec-1",
                                  "type": "IN",
                                  "quantity": 100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("IN"));

        mockMvc.perform(post("/api/items/" + id + "/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requesterId": "tec-1",
                                  "type": "OUT",
                                  "quantity": 30
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/items/" + id + "/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70));
    }
}
