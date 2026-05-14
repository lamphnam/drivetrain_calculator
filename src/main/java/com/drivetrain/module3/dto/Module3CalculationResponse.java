package com.drivetrain.module3.dto;

import com.drivetrain.domain.enums.ShaftCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Module3CalculationResponse(
        ResultInfo resultInfo,
        CaseInfo caseInfo,
        InputSummary inputSummary,
        MaterialSummary selectedMaterial,
        AllowableStressSummary allowableStresses,
        GearGeometrySummary gearGeometry,
        StressCheckSummary stressCheck,
        List<ShaftForceSummary> shaftForces,
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
            BigDecimal inputT1Nmm,
            BigDecimal inputN1Rpm,
            BigDecimal inputU2,
            BigDecimal serviceLifeHours
    ) {
    }

    public record MaterialSummary(
            Long materialId,
            String materialCode,
            String materialName,
            String heatTreatment,
            BigDecimal hbMin,
            BigDecimal hbMax,
            BigDecimal sigmaBMpa,
            BigDecimal sigmaChMpa
    ) {
    }

    public record AllowableStressSummary(
            BigDecimal allowableContactStressMpa,
            BigDecimal allowableBendingStressMpa
    ) {
    }

    public record GearGeometrySummary(
            BigDecimal reCalculated,
            BigDecimal de1Calculated,
            BigDecimal moduleMteSelected,
            Integer teethZ1,
            Integer teethZ2,
            BigDecimal actualRatioU2,
            BigDecimal widthBMm,
            BigDecimal diameterDm1Mm,
            BigDecimal diameterDm2Mm,
            BigDecimal coneAngleDelta1Deg,
            BigDecimal coneAngleDelta2Deg
    ) {
    }

    public record StressCheckSummary(
            BigDecimal sigmaHMpa,
            BigDecimal sigmaF1Mpa,
            BigDecimal sigmaF2Mpa,
            boolean contactStressPass,
            boolean bendingStressPass
    ) {
    }

    public record ShaftForceSummary(
            ShaftCode shaftCode,
            String shaftLabel,
            BigDecimal ftN,
            BigDecimal frN,
            BigDecimal faN
    ) {
    }
}
