package com.example.cropguard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Module 8: Assessor-only DTO for reviewing a claim. */
public record AssessClaimDto(
    @NotNull(message = "Damage percent is required")
    @Min(value = 0, message = "Damage percent cannot be negative")
    @Max(value = 100, message = "Damage percent cannot exceed 100")
    Double damagePercent,

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "(?i)APPROVED|REJECTED",
        message = "Decision must be APPROVED or REJECTED")
    String decision,

    String assessorNotes
) {}
