package com.example.cropguard.controller;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.dto.PlotDto;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.entity.Plot;
import com.example.cropguard.exception.BusinessException;
import com.example.cropguard.service.InsuredService;
import com.example.cropguard.service.PlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/plots")
public class PlotController {

    private final PlotService plotService;
    private final InsuredService insuredService;

    public PlotController(PlotService plotService, InsuredService insuredService) {
        this.plotService = plotService;
        this.insuredService = insuredService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<Plot> register(@Valid @RequestBody PlotDto dto, Principal principal) {
        // insuredId from the payload is ignored — a plot belongs to its creator
        Insured me = insuredService.findByEmail(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(plotService.register(dto, me.getId()));
    }

    @GetMapping
    public ResponseEntity<List<Plot>> list(
            @RequestParam(required = false) Long insuredId,
            @RequestParam(required = false) Bundesland bundesland,
            @RequestParam(required = false) CropType cropType,
            Principal principal) {
        Insured me = insuredService.findByEmail(principal.getName());
        if (!"ASSESSOR".equals(me.getRole())) {
            return ResponseEntity.ok(plotService.findByInsuredId(me.getId()));
        }
        if (insuredId != null) return ResponseEntity.ok(plotService.findByInsuredId(insuredId));
        if (bundesland != null) return ResponseEntity.ok(plotService.findByBundesland(bundesland));
        if (cropType != null) return ResponseEntity.ok(plotService.findByCropType(cropType));
        return ResponseEntity.ok(plotService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plot> getById(@PathVariable Long id, Principal principal) {
        Plot plot = plotService.findById(id);
        if (!plot.getInsured().getId().equals(insuredService.findByEmail(principal.getName()).getId())) {
            throw new BusinessException("Plot does not belong to the authenticated farmer");
        }
        return ResponseEntity.ok(plot);
    }
}
