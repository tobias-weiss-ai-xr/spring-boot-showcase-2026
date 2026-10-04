package com.example.cropguard.controller;

import com.example.cropguard.dto.HailEventDto;
import com.example.cropguard.entity.Claim;
import com.example.cropguard.entity.HailEvent;
import com.example.cropguard.service.HailEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hail-events")
public class HailEventController {

    private final HailEventService hailEventService;

    public HailEventController(HailEventService hailEventService) {
        this.hailEventService = hailEventService;
    }

    @PostMapping
    public ResponseEntity<HailEvent> register(@Valid @RequestBody HailEventDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hailEventService.register(dto));
    }

    @GetMapping
    public ResponseEntity<List<HailEvent>> list(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(required = false) HailEvent.Severity severity) {
        if (start != null && end != null)
            return ResponseEntity.ok(hailEventService.findByDateRange(start, end));
        if (severity != null)
            return ResponseEntity.ok(hailEventService.findBySeverity(severity));
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HailEvent> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hailEventService.findById(id));
    }

    @GetMapping("/{id}/claims")
    public ResponseEntity<List<Claim>> getClaims(@PathVariable Long id) {
        return ResponseEntity.ok(hailEventService.findClaimsForEvent(id));
    }
}
