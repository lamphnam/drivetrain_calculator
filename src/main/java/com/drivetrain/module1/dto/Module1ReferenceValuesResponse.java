package com.drivetrain.module1.dto;

import java.math.BigDecimal;

public record Module1ReferenceValuesResponse(
        Long constantSetId,
        String constantSetCode,
        String constantSetName,
        long availableMotorsCount,
        BigDecimal defaultBeltRatioU1,
        BigDecimal defaultGearboxRatioUh,
        BigDecimal defaultOverallEfficiency
) {
}
