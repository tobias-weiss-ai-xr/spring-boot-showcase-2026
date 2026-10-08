package com.example.cropguard.controller;

import com.example.cropguard.dto.AssessClaimDto;
import com.example.cropguard.dto.ClaimDto;
import com.example.cropguard.entity.Claim;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.exception.BusinessException;
import com.example.cropguard.modules.claims.ClaimService;
import com.example.cropguard.service.InsuredService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final InsuredService insuredService;

    public ClaimController(ClaimService claimService, InsuredService insuredService) {
        this.claimService = claimService;
        this.insuredService = insuredService;
    }

    @PostMapping
    public ResponseEntity<Claim> submit(@Valid @RequestBody ClaimDto dto, Principal principal) {
        Insured me = insuredService.findByEmail(principal.getName());
        return ResponseEntity.status(201).body(claimService.submit(dto, me.getId()));
    }

    @GetMapping
    public ResponseEntity<List<Claim>> list(@RequestParam(required = false) Long insuredId,
                                            Principal principal) {
        Insured me = insuredService.findByEmail(principal.getName());
        // farmers only ever see their own claims; assessors may query anyone or list all
        Long effective = "ASSESSOR".equals(me.getRole()) ? insuredId : me.getId();
        return ResponseEntity.ok(effective != null
            ? claimService.findByInsuredId(effective)
            : claimService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getById(@PathVariable Long id, Principal principal) {
        Claim claim = claimService.findById(id);
        enforceOwnership(claim.getPolicy().getPlot().getInsured().getId(), principal);
        return ResponseEntity.ok(claim);
    }

    @PutMapping("/{id}/assess")
    @PreAuthorize("hasRole('ASSESSOR')")
    public ResponseEntity<Claim> assess(@PathVariable Long id,
                                        @Valid @RequestBody AssessClaimDto dto,
                                        Principal principal) {
        return ResponseEntity.ok(claimService.assess(id, dto, principal.getName()));
    }

    private void enforceOwnership(Long ownerId, Principal principal) {
        if (!ownerId.equals(insuredService.findByEmail(principal.getName()).getId())) {
            throw new BusinessException("Claim does not belong to the authenticated farmer");
        }
    }
}
