package com.example.cropguard.dto;

import com.example.cropguard.domain.Deductible;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PolicyDto(
    Long id,
    @NotNull(message = "Coverage amount is required")
    @Min(value = 1000, message = "Coverage must be at least 1,000 EUR")
    Double coverageEur,

    Deductible deductible,

    String status,

    @NotNull(message = "Coverage start date is required")
    LocalDate coverageStart,

    @NotNull(message = "Coverage end date is required")
    LocalDate coverageEnd,

    @NotNull(message = "Plot ID is required")
    Long plotId
) {
    public String effectiveStatus() {
        return (status == null || status.isBlank()) ? "QUOTE" : status.toUpperCase();
    }
}
