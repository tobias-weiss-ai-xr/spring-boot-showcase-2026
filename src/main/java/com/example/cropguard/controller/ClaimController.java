package com.example.cropguard.controller;

import com.example.cropguard.dto.AssessClaimDto;
import com.example.cropguard.dto.ClaimDto;
import com.example.cropguard.entity.Claim;
import com.example.cropguard.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<Claim> submit(@Valid @RequestBody ClaimDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.submit(dto));
    }

    @GetMapping
    public ResponseEntity<List<Claim>> list(@RequestParam(required = false) Long insuredId) {
        return ResponseEntity.ok(insuredId != null
            ? claimService.findByInsuredId(insuredId)
            : claimService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.findById(id));
    }

    @PutMapping("/{id}/assess")
    @PreAuthorize("hasRole('ASSESSOR')")
    public ResponseEntity<Claim> assess(@PathVariable Long id,
                                        @Valid @RequestBody AssessClaimDto dto,
                                        java.security.Principal principal) {
        return ResponseEntity.ok(claimService.assess(id, dto, principal.getName()));
    }
}
