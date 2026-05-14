package com.drivetrain.fullflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FullFlowCalculationRequest(
        @NotNull(message = "requiredPowerKw is required")
        @Positive(message = "requiredPowerKw must be greater than zero")
        BigDecimal requiredPowerKw,

        @NotNull(message = "requiredOutputRpm is required")
        @Positive(message = "requiredOutputRpm must be greater than zero")
        BigDecimal requiredOutputRpm,

        @Positive(message = "constantSetId must be greater than zero")
        Long constantSetId,

        @Size(max = 100, message = "caseCode must not exceed 100 characters")
        String caseCode,

        @Size(max = 255, message = "caseName must not exceed 255 characters")
        String caseName,

        @Positive(message = "module3MaterialId must be greater than zero")
        Long module3MaterialId,

        @Positive(message = "module3ServiceLifeHours must be greater than zero")
        BigDecimal module3ServiceLifeHours,

        @Positive(message = "module4AllowableContactStressMpa must be greater than zero")
        BigDecimal module4AllowableContactStressMpa,

        @Positive(message = "module4AllowableBendingStressGear1Mpa must be greater than zero")
        BigDecimal module4AllowableBendingStressGear1Mpa,

        @Positive(message = "module4AllowableBendingStressGear2Mpa must be greater than zero")
        BigDecimal module4AllowableBendingStressGear2Mpa
) {
}
