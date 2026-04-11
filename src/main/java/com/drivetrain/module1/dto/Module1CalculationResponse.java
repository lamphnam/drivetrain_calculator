package com.drivetrain.module1.dto;

import com.drivetrain.domain.enums.ShaftCode;

import java.math.BigDecimal;
import java.util.List;

public record Module1CalculationResponse(
        BigDecimal totalEfficiency,
        BigDecimal requiredMotorPowerKw,
        BigDecimal preliminaryMotorRpmNsb,
        MotorResponse motor,
        TransmissionRatiosResponse transmissionRatios,
        List<ShaftResponse> shafts
) {

    public record MotorResponse(
            Long id,
            String motorCode,
            BigDecimal ratedPowerKw,
            BigDecimal ratedRpm
    ) {
    }

    public record TransmissionRatiosResponse(
            BigDecimal total,
            BigDecimal beltU1,
            BigDecimal gearboxUh,
            BigDecimal bevelGearU2,
            BigDecimal spurGearU3
    ) {
    }

    public record ShaftResponse(
            ShaftCode shaftCode,
            int sequenceNo,
            BigDecimal powerKw,
            BigDecimal rpm,
            BigDecimal torqueNmm
    ) {
    }
}
