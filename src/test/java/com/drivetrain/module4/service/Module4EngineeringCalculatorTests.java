package com.drivetrain.module4.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Module4EngineeringCalculatorTests {

    @Test
    void shouldMatchReferenceScaleForSpurGearExample() {
        Module4EngineeringCalculator.CalculationResult result = Module4EngineeringCalculator.calculate(
                new BigDecimal("251499.623"),
                new BigDecimal("258"),
                new BigDecimal("3.69"),
                new BigDecimal("600"),
                new BigDecimal("260"),
                new BigDecimal("260")
        );

        assertClose(result.centerDistanceAwMm(), 212.0, 3.0);
        assertEquals(4, result.moduleMSelected().intValue());
        assertEquals(23, result.teethZ1());
        assertEquals(85, result.teethZ2());
        assertClose(result.actualRatioU3(), 3.6957, 0.01);
        assertClose(result.widthBwMm(), 53.0, 1.5);
        assertTrue(result.diameterDw1Mm().doubleValue() > 0);
        assertTrue(result.diameterDw2Mm().doubleValue() > 0);
        assertTrue(result.ftN().doubleValue() > 0);
        assertTrue(result.frN().doubleValue() > 0);
        assertEquals(0, result.faN().compareTo(BigDecimal.ZERO.setScale(6)));
        assertTrue(result.contactStressPass());
        assertTrue(result.bendingStressGear1Pass());
        assertTrue(result.bendingStressGear2Pass());
    }

    @Test
    void shouldComputeCorrectGeometryRelationships() {
        Module4EngineeringCalculator.CalculationResult result = Module4EngineeringCalculator.calculate(
                new BigDecimal("251499.623"),
                new BigDecimal("258"),
                new BigDecimal("3.69"),
                new BigDecimal("600"),
                new BigDecimal("260"),
                new BigDecimal("260")
        );

        // dw2 should be approximately dw1 * actualRatioU3
        double expectedDw2 = result.diameterDw1Mm().doubleValue() * result.actualRatioU3().doubleValue();
        assertClose(result.diameterDw2Mm(), expectedDw2, 0.01);

        // Ft = 2 * T2 / dw1
        double expectedFt = 2 * 251499.623 / result.diameterDw1Mm().doubleValue();
        assertClose(result.ftN(), expectedFt, 1.0);

        // Fr = Ft * tan(20deg)
        double expectedFr = result.ftN().doubleValue() * Math.tan(Math.toRadians(20));
        assertClose(result.frN(), expectedFr, 1.0);
    }

    @Test
    void shouldThrowForNonPositiveT2() {
        assertThrows(IllegalArgumentException.class, () ->
                Module4EngineeringCalculator.calculate(
                        BigDecimal.ZERO,
                        new BigDecimal("258"),
                        new BigDecimal("3.69"),
                        new BigDecimal("600"),
                        new BigDecimal("260"),
                        new BigDecimal("260")
                )
        );
    }

    @Test
    void shouldThrowForNonPositiveN2() {
        assertThrows(IllegalArgumentException.class, () ->
                Module4EngineeringCalculator.calculate(
                        new BigDecimal("251499.623"),
                        new BigDecimal("-1"),
                        new BigDecimal("3.69"),
                        new BigDecimal("600"),
                        new BigDecimal("260"),
                        new BigDecimal("260")
                )
        );
    }

    @Test
    void shouldThrowForNonPositiveU3() {
        assertThrows(IllegalArgumentException.class, () ->
                Module4EngineeringCalculator.calculate(
                        new BigDecimal("251499.623"),
                        new BigDecimal("258"),
                        BigDecimal.ZERO,
                        new BigDecimal("600"),
                        new BigDecimal("260"),
                        new BigDecimal("260")
                )
        );
    }

    @Test
    void shouldThrowForNonPositiveAllowableContactStress() {
        assertThrows(IllegalArgumentException.class, () ->
                Module4EngineeringCalculator.calculate(
                        new BigDecimal("251499.623"),
                        new BigDecimal("258"),
                        new BigDecimal("3.69"),
                        new BigDecimal("-100"),
                        new BigDecimal("260"),
                        new BigDecimal("260")
                )
        );
    }

    @Test
    void shouldThrowForNonPositiveAllowableBendingStressGear1() {
        assertThrows(IllegalArgumentException.class, () ->
                Module4EngineeringCalculator.calculate(
                        new BigDecimal("251499.623"),
                        new BigDecimal("258"),
                        new BigDecimal("3.69"),
                        new BigDecimal("600"),
                        BigDecimal.ZERO,
                        new BigDecimal("260")
                )
        );
    }

    @Test
    void shouldThrowForNonPositiveAllowableBendingStressGear2() {
        assertThrows(IllegalArgumentException.class, () ->
                Module4EngineeringCalculator.calculate(
                        new BigDecimal("251499.623"),
                        new BigDecimal("258"),
                        new BigDecimal("3.69"),
                        new BigDecimal("600"),
                        new BigDecimal("260"),
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldThrowForNullInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                Module4EngineeringCalculator.calculate(
                        null,
                        new BigDecimal("258"),
                        new BigDecimal("3.69"),
                        new BigDecimal("600"),
                        new BigDecimal("260"),
                        new BigDecimal("260")
                )
        );
    }

    private void assertClose(BigDecimal actual, double expected, double tolerance) {
        assertTrue(
                Math.abs(actual.doubleValue() - expected) <= tolerance,
                () -> "Expected %s ± %s but got %s".formatted(expected, tolerance, actual)
        );
    }
}
