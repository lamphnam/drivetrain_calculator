package com.drivetrain.module4.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record Module4CalculationRequest(
        @NotNull(message = "designCaseId is required")
        @Positive(message = "designCaseId must be greater than zero")
        Long designCaseId,

        @Positive(message = "inputT2Nmm must be greater than zero")
        BigDecimal inputT2Nmm,

        @Positive(message = "inputN2Rpm must be greater than zero")
        BigDecimal inputN2Rpm,

        @Positive(message = "inputU3 must be greater than zero")
        BigDecimal inputU3,

        @Positive(message = "allowableContactStressMpa must be greater than zero")
        BigDecimal allowableContactStressMpa,

        @Positive(message = "allowableBendingStressGear1Mpa must be greater than zero")
        BigDecimal allowableBendingStressGear1Mpa,

        @Positive(message = "allowableBendingStressGear2Mpa must be greater than zero")
        BigDecimal allowableBendingStressGear2Mpa
) {
}
