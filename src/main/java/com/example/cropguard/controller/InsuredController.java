package com.example.cropguard.controller;

import com.example.cropguard.dto.InsuredDto;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.service.InsuredService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insureds")
public class InsuredController {

    private final InsuredService insuredService;

    public InsuredController(InsuredService insuredService) {
        this.insuredService = insuredService;
    }

    @PostMapping
    public ResponseEntity<Insured> register(@Valid @RequestBody InsuredDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(insuredService.register(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Insured> getById(@PathVariable Long id) {
        return ResponseEntity.ok(insuredService.findById(id));
    }
}
