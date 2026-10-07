package com.example.cropguard.controller;

import com.example.cropguard.config.AppProperties;
import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import com.example.cropguard.dto.PolicyDto;
import com.example.cropguard.entity.Plot;
import com.example.cropguard.entity.Policy;
import com.example.cropguard.security.JwtService;
import com.example.cropguard.service.PolicyService;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.service.InsuredService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @MockBean PolicyService policyService;
    @MockBean InsuredService insuredService;
    @MockBean JwtService jwtService;
    @MockBean AppProperties appProperties;

    @Test
    @WithMockUser(roles = "FARMER")
    void createPolicy_returns201() throws Exception {
        Insured me = mock(Insured.class);
        when(me.getId()).thenReturn(1L);
        when(insuredService.findByEmail(any())).thenReturn(me);
        Plot plot = new Plot(CropType.WHEAT, 25.0, Bundesland.HESSEN, 0.0, 0.0, "test", null);
        Policy policy = new Policy(25000.0, 1125.0, Deductible.TEN_PERCENT,
            Policy.Status.ACTIVE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), plot);
        when(policyService.create(any(), any())).thenReturn(policy);

        PolicyDto dto = new PolicyDto(null, 25000.0, Deductible.TEN_PERCENT,
            "ACTIVE", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), 1L);

        mockMvc.perform(post("/api/policies").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "FARMER")
    void createPolicy_negativeCoverage_returns400() throws Exception {
        PolicyDto dto = new PolicyDto(null, -100.0, Deductible.NONE,
            "ACTIVE", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), 1L);

        mockMvc.perform(post("/api/policies").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }
}
