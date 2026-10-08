package com.example.cropguard.controller;

import com.example.cropguard.config.AppProperties;
import com.example.cropguard.security.JwtService;
import com.example.cropguard.modules.billing.DwdRiskGridService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskMapController.class)
@Import(com.example.cropguard.config.SecurityConfig.class)
class RiskMapControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean DwdRiskGridService gridService;
    @MockBean JwtService jwtService;
    @MockBean AppProperties appProperties;

    @Test
    @WithMockUser(roles = "FARMER")
    void riskMap_authenticated_returnsDownsampledGrid() throws Exception {
        when(gridService.sampled(4)).thenReturn(new DwdRiskGridService.RiskMapData(
            3280414, 6104150, 5237500, 1000.0, 4, 2, 2, -999,
            new int[][] {{2, -999}, {5, 7}}));

        mockMvc.perform(get("/api/risk-map"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.step").value(4))
            .andExpect(jsonPath("$.ncols").value(2))
            .andExpect(jsonPath("$.nrows").value(2))
            .andExpect(jsonPath("$.cellsize").value(1000.0))
            .andExpect(jsonPath("$.values[0][0]").value(2))
            .andExpect(jsonPath("$.values[1][1]").value(7));
    }

    @Test
    void riskMap_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/api/risk-map"))
            .andExpect(status().isUnauthorized());
    }
}
