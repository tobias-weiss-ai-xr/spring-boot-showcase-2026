package com.example.cropguard.service;

import com.example.cropguard.domain.Deductible;
import com.example.cropguard.dto.AssessClaimDto;
import com.example.cropguard.dto.ClaimDto;
import com.example.cropguard.entity.Claim;
import com.example.cropguard.entity.Policy;
import com.example.cropguard.exception.BusinessException;
import com.example.cropguard.exception.ResourceNotFoundException;
import com.example.cropguard.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyService policyService;
    private final PremiumCalculator premiumCalculator;

    public ClaimService(ClaimRepository claimRepository, PolicyService policyService,
                        PremiumCalculator premiumCalculator) {
        this.claimRepository = claimRepository;
        this.policyService = policyService;
        this.premiumCalculator = premiumCalculator;
    }

    public Claim submit(ClaimDto dto, Long requesterInsuredId) {
        Policy policy = policyService.findById(dto.policyId());

        if (!policy.getPlot().getInsured().getId().equals(requesterInsuredId)) {
            throw new BusinessException("Policy does not belong to the authenticated farmer");
        }

        if (policy.getStatus() != Policy.Status.ACTIVE) {
            throw new BusinessException(
                "Cannot file claim on non-active policy (status: " + policy.getStatus() + ")");
        }

        LocalDate today = LocalDate.now();
        if (dto.damageDate().isBefore(policy.getCoverageStart())) {
            throw new BusinessException("Damage date is before policy coverage start");
        }
        if (dto.damageDate().isAfter(policy.getCoverageEnd())) {
            throw new BusinessException("Damage date is after policy coverage end");
        }
        if (dto.damageDate().isAfter(today)) {
            throw new BusinessException("Damage date cannot be in the future");
        }

        Claim claim = new Claim(
            dto.damageDate(),
            dto.damageDescription(),
            Claim.Status.SUBMITTED,
            policy
        );
        claim.setHailEventId(dto.hailEventId());
        return claimRepository.save(claim);
    }

    public Claim assess(Long claimId, AssessClaimDto dto, String assessedBy) {
        Claim claim = findById(claimId);
        if (claim.getStatus() != Claim.Status.SUBMITTED
            && claim.getStatus() != Claim.Status.UNDER_REVIEW) {
            throw new BusinessException(
                "Claim cannot be assessed in status: " + claim.getStatus());
        }

        Policy policy = claim.getPolicy();
        Deductible deductible = policy.getDeductible();
        double payout = premiumCalculator.calculatePayout(
            policy.getCoverageEur(), dto.damagePercent(), deductible);

        claim.setDamagePercent(dto.damagePercent());
        claim.setPayoutEur(payout);
        claim.setAssessorNotes(dto.assessorNotes());
        claim.setAssessedBy(assessedBy);
        claim.setStatus(Claim.Status.valueOf(dto.decision().toUpperCase()));
        return claimRepository.save(claim);
    }

    public List<Claim> findByInsuredId(Long insuredId) {
        return claimRepository.findByPolicyPlotInsuredId(insuredId);
    }

    public List<Claim> findAll() {
        return claimRepository.findAll();
    }

    public List<Claim> findByHailEventId(Long hailEventId) {
        return claimRepository.findByHailEventId(hailEventId);
    }

    public Claim findById(Long id) {
        return claimRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Claim", id));
    }
}
