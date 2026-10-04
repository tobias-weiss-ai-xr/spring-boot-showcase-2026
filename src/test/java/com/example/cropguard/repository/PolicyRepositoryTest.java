package com.example.cropguard.repository;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.entity.Plot;
import com.example.cropguard.entity.Policy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PolicyRepositoryTest {

    @Autowired PolicyRepository policyRepository;
    @Autowired PlotRepository plotRepository;
    @Autowired InsuredRepository insuredRepository;

    @Test
    void saveAndFindByPlotInsuredId() {
        Insured farmer = insuredRepository.save(
            new Insured("Test Farmer", "test@farm.de", "hash", "FARMER", Bundesland.HESSEN));
        Plot plot = plotRepository.save(
            new Plot(CropType.WHEAT, 25.0, Bundesland.HESSEN, 0.0, 0.0, "test field", farmer));
        Policy policy = policyRepository.save(new Policy(
            25000.0, 1125.0, Deductible.TEN_PERCENT,
            Policy.Status.ACTIVE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31), plot));

        List<Policy> found = policyRepository.findByPlotInsuredId(farmer.getId());
        assertEquals(1, found.size());
        assertEquals(policy.getId(), found.get(0).getId());
    }
}
