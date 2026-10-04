package com.example.cropguard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Module 8: Assessor-only DTO for reviewing a claim. */
public record AssessClaimDto(
    @NotNull(message = "Damage percent is required")
    @Min(value = 0, message = "Damage percent cannot be negative")
    @Max(value = 100, message = "Damage percent cannot exceed 100")
    Double damagePercent,

    @NotBlank(message = "Decision is required")
    String decision,  // APPROVED or REJECTED

    String assessorNotes
) {}
