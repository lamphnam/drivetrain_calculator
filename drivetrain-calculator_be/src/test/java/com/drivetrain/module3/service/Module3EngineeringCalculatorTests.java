package com.drivetrain.module3.service;

import com.drivetrain.domain.entity.GearMaterial;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Module3EngineeringCalculatorTests {

    @Test
    void shouldMatchReferenceScaleForBevelGearExample() {
        GearMaterial material = GearMaterial.builder()
                .materialCode("C40XH_QT")
                .materialName("Steel C40XH")
                .heatTreatment("Quenched and tempered")
                .hbMin(new BigDecimal("235"))
                .hbMax(new BigDecimal("262"))
                .sigmaBMpa(new BigDecimal("850"))
                .sigmaChMpa(new BigDecimal("650"))
                .build();

        Module3EngineeringCalculator.CalculationResult result = Module3EngineeringCalculator.calculate(
                new BigDecimal("83851.991"),
                new BigDecimal("812"),
                new BigDecimal("3.14"),
                new BigDecimal("43200"),
                material
        );

        assertClose(result.moduleMteSelected(), 3.0, 0.0001);
        assertTrue(result.teethZ1() == 27);
        assertTrue(result.teethZ2() == 85);
        assertClose(result.widthBMm(), 38.0, 0.0001);
        assertClose(result.diameterDm1Mm(), 69.458, 0.0500);
        assertClose(result.diameterDm2Mm(), 218.663, 0.1500);
        assertClose(result.coneAngleDelta1Deg(), 17.62, 0.0500);
        assertClose(result.coneAngleDelta2Deg(), 72.38, 0.0500);
        assertClose(result.shaft1FtN(), 2414.466, 3.0000);
        assertClose(result.shaft1FrN(), 837.565, 2.0000);
        assertClose(result.shaft1FaN(), 266.013, 2.0000);
        assertFalse(result.usedFallbackCandidate());
        assertTrue(result.contactStressPass());
        assertTrue(result.bendingStressPass());
    }

    private void assertClose(BigDecimal actual, double expected, double tolerance) {
        assertTrue(
                Math.abs(actual.doubleValue() - expected) <= tolerance,
                () -> "Expected %s ± %s but got %s".formatted(expected, tolerance, actual)
        );
    }
}
