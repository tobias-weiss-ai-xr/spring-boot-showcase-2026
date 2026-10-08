package com.example.cropguard.modules.claims;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import com.example.cropguard.dto.AssessClaimDto;
import com.example.cropguard.dto.ClaimDto;
import com.example.cropguard.entity.Claim;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.entity.Plot;
import com.example.cropguard.entity.Policy;
import com.example.cropguard.exception.BusinessException;
import com.example.cropguard.modules.billing.PremiumCalculator;
import com.example.cropguard.modules.policy.PolicyService;
import com.example.cropguard.repository.ClaimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock ClaimRepository claimRepository;
    @Mock PolicyService policyService;
    @Mock PremiumCalculator premiumCalculator;
    @InjectMocks ClaimService claimService;

    @Test
    void submit_onActivePolicy_returnsSubmittedClaim() {
        Plot plot = plotOwnedBy(7L);
        Policy policy = new Policy(25000.0, 1125.0, Deductible.TEN_PERCENT,
            Policy.Status.ACTIVE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), plot);
        when(policyService.findById(1L)).thenReturn(policy);
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ClaimDto dto = new ClaimDto(null, LocalDate.of(2026, 7, 15),
            "Hail damage on wheat field", "SUBMITTED", null, 1L);

        Claim result = claimService.submit(dto, 7L);
        assertEquals(Claim.Status.SUBMITTED, result.getStatus());
    }

    @Test
    void submit_onForeignPolicy_throwsBusinessException() {
        Plot plot = plotOwnedBy(7L);
        Policy policy = new Policy(25000.0, 1125.0, Deductible.TEN_PERCENT,
            Policy.Status.ACTIVE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), plot);
        when(policyService.findById(1L)).thenReturn(policy);

        ClaimDto dto = new ClaimDto(null, LocalDate.of(2026, 7, 15),
            "Hail damage on wheat field", "SUBMITTED", null, 1L);

        assertThrows(BusinessException.class, () -> claimService.submit(dto, 8L));
    }

    /** Plot entity has no setters — the owner is mocked in. */
    private Plot plotOwnedBy(long insuredId) {
        Insured owner = mock(Insured.class);
        when(owner.getId()).thenReturn(insuredId);
        return new Plot(CropType.WHEAT, 25.0, Bundesland.HESSEN, 0.0, 0.0, "test", owner);
    }

    @Test
    void submit_onExpiredPolicy_throwsBusinessException() {
        Plot plot = plotOwnedBy(7L);
        Policy policy = new Policy(25000.0, 1125.0, Deductible.TEN_PERCENT,
            Policy.Status.EXPIRED, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 12, 31), plot);
        when(policyService.findById(1L)).thenReturn(policy);

        ClaimDto dto = new ClaimDto(null, LocalDate.of(2026, 7, 15),
            "Hail damage on wheat field", "SUBMITTED", null, 1L);

        assertThrows(BusinessException.class, () -> claimService.submit(dto, 7L));
    }

    @Test
    void assess_validClaim_setsPayoutAndStatus() {
        Plot plot = new Plot(CropType.WHEAT, 25.0, Bundesland.HESSEN, 0.0, 0.0, "test", null);
        Policy policy = new Policy(25000.0, 1125.0, Deductible.TEN_PERCENT,
            Policy.Status.ACTIVE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), plot);
        Claim claim = new Claim(LocalDate.of(2026, 7, 15), "Hail damage", Claim.Status.SUBMITTED, policy);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(premiumCalculator.calculatePayout(anyDouble(), anyDouble(), any())).thenReturn(18000.0);
        when(claimRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AssessClaimDto dto = new AssessClaimDto(80.0, "APPROVED", "Total loss");
        Claim result = claimService.assess(1L, dto, "lisa@cropguard.de");

        assertEquals(Claim.Status.APPROVED, result.getStatus());
        assertEquals(18000.0, result.getPayoutEur());
        assertEquals("lisa@cropguard.de", result.getAssessedBy());
    }
}
