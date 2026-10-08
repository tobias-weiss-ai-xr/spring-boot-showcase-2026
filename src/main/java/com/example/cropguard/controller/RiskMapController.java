package com.example.cropguard.controller;

import com.example.cropguard.modules.billing.DwdRiskGridService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the DWD drought index grid downsampled for the frontend risk map.
 * Authentication required (same as the rest of the API).
 */
@RestController
@RequestMapping("/api/risk-map")
public class RiskMapController {

    private final DwdRiskGridService gridService;

    public RiskMapController(DwdRiskGridService gridService) {
        this.gridService = gridService;
    }

    @GetMapping
    public DwdRiskGridService.RiskMapData riskMap(
            @RequestParam(defaultValue = "4") int step) {
        return gridService.sampled(step);
    }
}
