package com.drivetrain.module1.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record Module1CalculationHistoryItemResponse(
        Long designCaseId,
        Long resultId,
        String moduleLabel,
        String caseCode,
        String caseName,
        BigDecimal requiredPowerKw,
        BigDecimal requiredOutputRpm,
        String selectedMotorCode,
        String selectedMotorDisplayName,
        Instant savedAt,
        Instant updatedAt
) {
}
