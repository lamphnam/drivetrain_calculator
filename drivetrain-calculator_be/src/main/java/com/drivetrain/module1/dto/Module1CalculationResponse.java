package com.drivetrain.module1.dto;

import com.drivetrain.domain.enums.ShaftCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Module1CalculationResponse(
        ResultInfo resultInfo,
        CaseInfo caseInfo,
        InputSummary inputSummary,
        ReferenceSummary referenceSummary,
        SelectedMotorSummary selectedMotor,
        BigDecimal systemEfficiency,
        BigDecimal requiredMotorPowerKw,
        BigDecimal preliminaryMotorRpmNsb,
        TransmissionRatiosSummary transmissionRatios,
        List<ShaftStateSummary> shaftStates,
        List<String> calculationNotes
) {

    public record ResultInfo(
            Long resultId,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CaseInfo(
            Long designCaseId,
            String caseCode,
            String caseName,
            String status
    ) {
    }

    public record InputSummary(
            BigDecimal requiredPowerKw,
            BigDecimal requiredOutputRpm
    ) {
    }

    public record ReferenceSummary(
            Long constantSetId,
            String constantSetCode,
            String constantSetName,
            long availableMotorsCount,
            BigDecimal defaultBeltRatioU1,
            BigDecimal defaultGearboxRatioUh
    ) {
    }

    public record SelectedMotorSummary(
            Long motorId,
            String motorCode,
            String displayName,
            String manufacturer,
            String description,
            BigDecimal ratedPowerKw,
            BigDecimal ratedRpm
    ) {
    }

    public record TransmissionRatiosSummary(
            BigDecimal overallRatio,
            BigDecimal beltRatioU1,
            BigDecimal gearboxRatioUh,
            BigDecimal bevelRatioU2,
            BigDecimal spurRatioU3
    ) {
    }

    public record ShaftStateSummary(
            ShaftCode shaftCode,
            String shaftLabel,
            int sequenceNo,
            BigDecimal powerKw,
            BigDecimal rpm,
            BigDecimal torqueNmm
    ) {
    }
}
