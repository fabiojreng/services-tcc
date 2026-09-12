package br.edu.ifma.labmanager.catalog.infra;

import br.edu.ifma.labmanager.catalog.application.ports.IdentityGateway;
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
class CatalogApplicationTests {

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
    void registraEConsultaLaboratorio() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/laboratories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requesterId": "tec-1",
                                  "name": "Lab Redes",
                                  "capacity": 20,
                                  "opensAt": "08:00:00",
                                  "closesAt": "18:00:00",
                                  "openDays": ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lab Redes"))
                .andReturn();

        String json = created.getResponse().getContentAsString();
        String id = json.replaceAll("(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/laboratories/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opensAt").value("08:00:00"))
                .andExpect(jsonPath("$.capacity").value(20));
    }
}
