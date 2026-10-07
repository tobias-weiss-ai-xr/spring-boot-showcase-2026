package com.example.cropguard.controller;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.exception.BusinessException;
import com.example.cropguard.service.PremiumCalculator;
import com.example.cropguard.dto.PolicyDto;
import com.example.cropguard.entity.Policy;
import com.example.cropguard.service.PolicyService;
import com.example.cropguard.service.InsuredService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;
    private final InsuredService insuredService;

    public PolicyController(PolicyService policyService, InsuredService insuredService) {
        this.policyService = policyService;
        this.insuredService = insuredService;
    }

    @PostMapping
    public ResponseEntity<Policy> create(@Valid @RequestBody PolicyDto dto, Principal principal) {
        Insured me = insuredService.findByEmail(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.create(dto, me.getId()));
    }

    @GetMapping
    public ResponseEntity<List<Policy>> list(@RequestParam(required = false) Long insuredId,
                                             Principal principal) {
        Insured me = insuredService.findByEmail(principal.getName());
        // farmers only ever see their own policies; assessors may query anyone or list all
        Long effective = "ASSESSOR".equals(me.getRole()) ? insuredId : me.getId();
        return ResponseEntity.ok(effective != null
            ? policyService.findByInsuredId(effective)
            : policyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Policy> getById(@PathVariable Long id, Principal principal) {
        Policy policy = policyService.findById(id);
        if (!policy.getPlot().getInsured().getId()
                .equals(insuredService.findByEmail(principal.getName()).getId())) {
            throw new BusinessException("Policy does not belong to the authenticated farmer");
        }
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/quote")
    public ResponseEntity<PremiumCalculator.PremiumResult> quote(
            @RequestParam CropType cropType,
            @RequestParam double hectares,
            @RequestParam Bundesland bundesland,
            @RequestParam(required = false, defaultValue = "NONE") Deductible deductible,
            @RequestParam double coverageEur,
            @RequestParam(required = false) Double coordinateE,
            @RequestParam(required = false) Double coordinateN) {
        return ResponseEntity.ok(
            policyService.quote(cropType, hectares, bundesland, deductible, coverageEur,
                coordinateE, coordinateN));
    }
}
