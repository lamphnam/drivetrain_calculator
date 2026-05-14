package com.drivetrain.module4.dto;

import com.drivetrain.domain.enums.ShaftCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Module4CalculationResponse(
        ResultInfo resultInfo,
        CaseInfo caseInfo,
        InputSummary inputSummary,
        SpurGearGeometry spurGearGeometry,
        DerivedFactors derivedFactors,
        StressCheck stressCheck,
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
            BigDecimal inputT2Nmm,
            BigDecimal inputN2Rpm,
            BigDecimal inputU3,
            BigDecimal allowableContactStressMpa,
            BigDecimal allowableBendingStressGear1Mpa,
            BigDecimal allowableBendingStressGear2Mpa
    ) {
    }

    public record SpurGearGeometry(
            BigDecimal centerDistanceAwMm,
            BigDecimal moduleMSelected,
            Integer teethZ1,
            Integer teethZ2,
            BigDecimal actualRatioU3,
            BigDecimal ratioErrorPercent,
            BigDecimal diameterDw1Mm,
            BigDecimal diameterDw2Mm,
            BigDecimal widthBwMm
    ) {
    }

    public record DerivedFactors(
            BigDecimal epsilonAlpha,
            BigDecimal zEpsilon,
            BigDecimal yEpsilon,
            BigDecimal yF1,
            BigDecimal yF2,
            BigDecimal loadFactorKh,
            BigDecimal loadFactorKf
    ) {
    }

    public record StressCheck(
            BigDecimal sigmaHMpa,
            BigDecimal sigmaF1Mpa,
            BigDecimal sigmaF2Mpa,
            boolean contactStressPass,
            boolean bendingStressGear1Pass,
            boolean bendingStressGear2Pass
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
