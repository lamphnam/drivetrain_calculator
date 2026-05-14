package com.drivetrain.module3.dto;

import java.math.BigDecimal;

public record GearMaterialReferenceResponse(
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
