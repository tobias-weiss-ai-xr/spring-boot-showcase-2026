package com.example.cropguard.service;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import com.example.cropguard.dto.PolicyDto;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.entity.Plot;
import com.example.cropguard.entity.Policy;
import com.example.cropguard.exception.BusinessException;
import com.example.cropguard.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock PolicyRepository policyRepository;
    @Mock PlotService plotService;
    @Mock PremiumCalculator premiumCalculator;
    @InjectMocks PolicyService policyService;

    @Test
    void create_validPolicy_returnsSavedPolicy() {
        Plot plot = plotOwnedBy(7L);
        when(plotService.findById(1L)).thenReturn(plot);
        var premium = new PremiumCalculator.PremiumResult(1125.0, 1125.0, 1.0, 0.85, 25000.0, 2, 1.0);
        when(premiumCalculator.calculate(any(), anyDouble(), any(), any(), anyDouble(), any(), any())).thenReturn(premium);
        when(policyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // client sends a bogus status — the server must own the lifecycle and create ACTIVE
        PolicyDto dto = new PolicyDto(null, 25000.0, Deductible.TEN_PERCENT,
            "CANCELLED", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), 1L);

        Policy result = policyService.create(dto, 7L);
        assertNotNull(result);
        assertEquals(25000.0, result.getCoverageEur());
        assertEquals(Policy.Status.ACTIVE, result.getStatus());
    }

    @Test
    void create_onForeignPlot_throwsBusinessException() {
        Plot plot = plotOwnedBy(7L);
        when(plotService.findById(1L)).thenReturn(plot);

        PolicyDto dto = new PolicyDto(null, 25000.0, Deductible.TEN_PERCENT,
            "ACTIVE", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), 1L);

        assertThrows(BusinessException.class, () -> policyService.create(dto, 8L));
    }

    /** Plot entity has no setters — the owner is mocked in. */
    private Plot plotOwnedBy(long insuredId) {
        Insured owner = mock(Insured.class);
        when(owner.getId()).thenReturn(insuredId);
        return new Plot(CropType.WHEAT, 25.0, Bundesland.HESSEN, 0.0, 0.0, "test", owner);
    }

    @Test
    void create_coverageEndBeforeStart_throwsBusinessException() {
        Plot plot = plotOwnedBy(7L);
        when(plotService.findById(1L)).thenReturn(plot);

        PolicyDto dto = new PolicyDto(null, 25000.0, Deductible.TEN_PERCENT,
            "ACTIVE", LocalDate.of(2026, 12, 31), LocalDate.of(2026, 3, 1), 1L);

        assertThrows(BusinessException.class, () -> policyService.create(dto, 7L));
    }
}
