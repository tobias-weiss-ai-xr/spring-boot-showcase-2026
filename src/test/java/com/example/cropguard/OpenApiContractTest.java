package com.example.cropguard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Published API contract (ACE pattern): /v3/api-docs serves OpenAPI 3 with build version and core module paths. */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiContractTest {

    @Autowired MockMvc mockMvc;

    @Test
    void apiDocs_servesOpenApi3WithCoreModulePaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.openapi").value(matchesPattern("^3\\.\\d+\\.\\d+")))
            .andExpect(jsonPath("$.info.version").value(matchesPattern("^\\d+\\.\\d+\\.\\d+")))
            .andExpect(jsonPath("$.paths['/api/policies']").exists())
            .andExpect(jsonPath("$.paths['/api/claims']").exists())
            .andExpect(jsonPath("$.paths['/api/plots']").exists());
    }

    @Test
    void apiDocs_exposesModuleTags() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(jsonPath("$.tags[?(@.name == 'policy')]").exists())
            .andExpect(jsonPath("$.tags[?(@.name == 'billing')]").exists())
            .andExpect(jsonPath("$.tags[?(@.name == 'claims')]").exists());
    }
}
