package com.example.cropguard.dto;

import com.example.cropguard.entity.HailEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record HailEventDto(
    Long id,
    @NotNull(message = "Event date is required")
    LocalDate eventDate,

    @NotBlank(message = "Affected Bundeslaender are required")
    String affectedBundeslaender,

    @NotNull(message = "Severity is required")
    HailEvent.Severity severity,

    String description,

    Integer hailstoneDiameterMm
) {}
