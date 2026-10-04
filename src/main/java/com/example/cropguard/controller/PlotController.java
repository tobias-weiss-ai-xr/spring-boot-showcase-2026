package com.example.cropguard.controller;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.dto.PlotDto;
import com.example.cropguard.entity.Plot;
import com.example.cropguard.service.PlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/plots")
public class PlotController {

    private final PlotService plotService;

    public PlotController(PlotService plotService) {
        this.plotService = plotService;
    }

    @PostMapping
    public ResponseEntity<Plot> register(@Valid @RequestBody PlotDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plotService.register(dto));
    }

    @GetMapping
    public ResponseEntity<List<Plot>> list(
            @RequestParam(required = false) Long insuredId,
            @RequestParam(required = false) Bundesland bundesland,
            @RequestParam(required = false) CropType cropType) {
        if (insuredId != null) return ResponseEntity.ok(plotService.findByInsuredId(insuredId));
        if (bundesland != null) return ResponseEntity.ok(plotService.findByBundesland(bundesland));
        if (cropType != null) return ResponseEntity.ok(plotService.findByCropType(cropType));
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plot> getById(@PathVariable Long id) {
        return ResponseEntity.ok(plotService.findById(id));
    }
}
