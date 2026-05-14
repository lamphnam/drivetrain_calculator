package com.drivetrain.module3.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record Module3CalculationRequest(
        @NotNull(message = "designCaseId is required")
        @Positive(message = "designCaseId must be greater than zero")
        Long designCaseId,

        @Positive(message = "inputT1Nmm must be greater than zero")
        BigDecimal inputT1Nmm,

        @Positive(message = "inputN1Rpm must be greater than zero")
        BigDecimal inputN1Rpm,

        @Positive(message = "inputU2 must be greater than zero")
        BigDecimal inputU2,

        @Positive(message = "serviceLifeHours must be greater than zero")
        BigDecimal serviceLifeHours,

        @NotNull(message = "materialId is required")
        @Positive(message = "materialId must be greater than zero")
        Long materialId
) {
}
