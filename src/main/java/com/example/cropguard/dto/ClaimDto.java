package com.example.cropguard.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ClaimDto(
    Long id,
    @NotNull(message = "Damage date is required")
    LocalDate damageDate,

    @NotBlank(message = "Damage description is required")
    @Size(min = 10, max = 3000, message = "Description must be 10–3000 characters")
    String damageDescription,

    String status,

    Long hailEventId,

    @NotNull(message = "Policy ID is required")
    Long policyId
) {
    public String effectiveStatus() {
        return (status == null || status.isBlank()) ? "SUBMITTED" : status.toUpperCase();
    }
}
