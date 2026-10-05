package com.example.cropguard.controller;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import com.example.cropguard.service.PremiumCalculator;
import com.example.cropguard.dto.PolicyDto;
import com.example.cropguard.entity.Policy;
import com.example.cropguard.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    public ResponseEntity<Policy> create(@Valid @RequestBody PolicyDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<Policy>> list(@RequestParam Long insuredId) {
        return ResponseEntity.ok(policyService.findByInsuredId(insuredId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Policy> getById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.findById(id));
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
