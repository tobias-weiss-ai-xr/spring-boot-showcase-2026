package com.example.cropguard.modules.policy;

import com.example.cropguard.domain.Deductible;
import com.example.cropguard.dto.PolicyDto;
import com.example.cropguard.entity.Plot;
import com.example.cropguard.entity.Policy;
import com.example.cropguard.exception.BusinessException;
import com.example.cropguard.exception.ResourceNotFoundException;
import com.example.cropguard.modules.billing.PremiumCalculator;
import com.example.cropguard.repository.PolicyRepository;
import com.example.cropguard.service.PlotService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PlotService plotService;
    private final PremiumCalculator premiumCalculator;

    public PolicyService(PolicyRepository policyRepository, PlotService plotService,
                         PremiumCalculator premiumCalculator) {
        this.policyRepository = policyRepository;
        this.plotService = plotService;
        this.premiumCalculator = premiumCalculator;
    }

    public Policy create(PolicyDto dto, Long requesterInsuredId) {
        Plot plot = plotService.findById(dto.plotId());

        if (!plot.getInsured().getId().equals(requesterInsuredId)) {
            throw new BusinessException("Plot does not belong to the authenticated farmer");
        }

        Deductible deductible = dto.deductible() != null ? dto.deductible() : Deductible.NONE;

        if (dto.coverageEnd().isBefore(dto.coverageStart())) {
            throw new BusinessException("Coverage end must be after start");
        }

        var premium = premiumCalculator.calculate(
            plot.getCropType(), plot.getHectares(), plot.getBundesland(),
            deductible, dto.coverageEur(),
            plot.getCoordinateE(), plot.getCoordinateN());

        if (dto.coverageEur() < premium.premiumEur() * 10) {
            throw new BusinessException(
                "Coverage must be at least 10x the premium (min: "
                    + (premium.premiumEur() * 10) + " EUR)");
        }

        Policy policy = new Policy(
            dto.coverageEur(),
            premium.premiumEur(),
            deductible,
            Policy.Status.ACTIVE,  // server-owned: clients cannot create EXPIRED/CANCELLED policies
            dto.coverageStart(),
            dto.coverageEnd(),
            plot
        );
        return policyRepository.save(policy);
    }

    public PremiumCalculator.PremiumResult quote(
            com.example.cropguard.domain.CropType cropType,
            double hectares,
            com.example.cropguard.domain.Bundesland bundesland,
            Deductible deductible,
            double coverageEur,
            Double coordinateE, Double coordinateN) {
        return premiumCalculator.calculate(cropType, hectares, bundesland, deductible, coverageEur,
            coordinateE, coordinateN);
    }

    public List<Policy> findByInsuredId(Long insuredId) {
        return policyRepository.findByPlotInsuredId(insuredId);
    }

    public List<Policy> findAll() {
        return policyRepository.findAll();
    }

    public Policy findById(Long id) {
        return policyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Policy", id));
    }
}
