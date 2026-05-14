package com.drivetrain.module1.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record Module1CalculationRequest(
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
        String caseName
) {
}
