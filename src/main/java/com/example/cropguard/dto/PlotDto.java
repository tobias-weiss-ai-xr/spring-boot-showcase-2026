package com.example.cropguard.dto;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlotDto(
    Long id,
    @NotNull(message = "Crop type is required")
    CropType cropType,

    @NotNull(message = "Hectares are required")
    @Min(value = 1, message = "Plot must be at least 0.1 hectares")
    Double hectares,

    @NotNull(message = "Bundesland is required")
    Bundesland bundesland,

    Double coordinateE,
    Double coordinateN,

    @NotBlank(message = "Location description is required")
    String locationDescription,

    @NotNull(message = "Insured ID is required")
    Long insuredId
) {}
