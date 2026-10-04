package com.example.cropguard.controller;

import com.example.cropguard.config.AppProperties;
import com.example.cropguard.dto.AssessClaimDto;
import com.example.cropguard.dto.ClaimDto;
import com.example.cropguard.entity.Claim;
import com.example.cropguard.security.JwtService;
import com.example.cropguard.service.ClaimService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaimController.class)
@Import(com.example.cropguard.config.SecurityConfig.class)
class ClaimControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @MockBean ClaimService claimService;
    @MockBean JwtService jwtService;
    @MockBean AppProperties appProperties;

    @Test
    @WithMockUser(roles = "FARMER")
    void submitClaim_returns201() throws Exception {
        Claim claim = new Claim(LocalDate.of(2026, 7, 15), "Hail damage", Claim.Status.SUBMITTED, null);
        when(claimService.submit(any())).thenReturn(claim);

        ClaimDto dto = new ClaimDto(null, LocalDate.of(2026, 7, 15),
            "Hail damage on wheat field", "SUBMITTED", null, 1L);

        mockMvc.perform(post("/api/claims").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "FARMER")
    void submitClaim_blankDescription_returns400() throws Exception {
        ClaimDto dto = new ClaimDto(null, LocalDate.of(2026, 7, 15),
            "short", "SUBMITTED", null, 1L);

        mockMvc.perform(post("/api/claims").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ASSESSOR")
    void assessClaim_asAssessor_returns200() throws Exception {
        Claim claim = new Claim(LocalDate.of(2026, 7, 15), "Hail damage", Claim.Status.APPROVED, null);
        claim.setDamagePercent(80.0);
        claim.setPayoutEur(18000.0);
        when(claimService.assess(eq(1L), any(), eq("user"))).thenReturn(claim);

        AssessClaimDto dto = new AssessClaimDto(80.0, "APPROVED", "Total loss");

        mockMvc.perform(put("/api/claims/1/assess").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FARMER")
    void assessClaim_asFarmer_returns403() throws Exception {
        AssessClaimDto dto = new AssessClaimDto(80.0, "APPROVED", "Total loss");

        mockMvc.perform(put("/api/claims/1/assess").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }
}
