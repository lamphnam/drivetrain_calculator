package com.drivetrain.module3.service;

import com.drivetrain.domain.entity.GearMaterial;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

final class Module3EngineeringCalculator {

    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);
    private static final int SCALE = 6;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
    private static final BigDecimal NINETY = BigDecimal.valueOf(90);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal CONTACT_BASE_CYCLES = BigDecimal.valueOf(30_000_000L);
    private static final BigDecimal BENDING_BASE_CYCLES = BigDecimal.valueOf(4_000_000L);
    private static final BigDecimal MIN_CONTACT_LIFE_FACTOR = BigDecimal.valueOf(0.85);
    private static final BigDecimal MIN_BENDING_LIFE_FACTOR = BigDecimal.valueOf(0.80);
    private static final BigDecimal MAX_LIFE_FACTOR = BigDecimal.valueOf(1.15);
    private static final BigDecimal CONTACT_MATERIAL_FACTOR = BigDecimal.valueOf(274);
    private static final BigDecimal CONTACT_GEOMETRY_FACTOR = BigDecimal.valueOf(1.764);
    private static final BigDecimal CONTACT_LOAD_FACTOR = BigDecimal.valueOf(1.10);
    private static final BigDecimal BENDING_LOAD_FACTOR = BigDecimal.valueOf(1.15);
    private static final BigDecimal WIDTH_FACTOR = BigDecimal.valueOf(0.285);
    private static final BigDecimal RE_ESTIMATE_FACTOR = BigDecimal.valueOf(4.46);
    private static final BigDecimal TANGENT_20 = BigDecimal.valueOf(Math.tan(Math.toRadians(20)));
    private static final List<BigDecimal> STANDARD_MODULES = List.of(
            BigDecimal.valueOf(2.0),
            BigDecimal.valueOf(2.5),
            BigDecimal.valueOf(3.0),
            BigDecimal.valueOf(4.0),
            BigDecimal.valueOf(5.0),
            BigDecimal.valueOf(6.0),
            BigDecimal.valueOf(8.0),
            BigDecimal.valueOf(10.0),
            BigDecimal.valueOf(12.0)
    );

    private Module3EngineeringCalculator() {
    }

    static CalculationResult calculate(
            BigDecimal inputT1Nmm,
            BigDecimal inputN1Rpm,
            BigDecimal inputU2,
            BigDecimal serviceLifeHours,
            GearMaterial material
    ) {
        BigDecimal torque = requirePositive(inputT1Nmm, "inputT1Nmm");
        BigDecimal speed = requirePositive(inputN1Rpm, "inputN1Rpm");
        BigDecimal requestedRatio = requirePositive(inputU2, "inputU2");
        BigDecimal lifeHours = requirePositive(serviceLifeHours, "serviceLifeHours");
        BigDecimal hbMin = requirePositive(material.getHbMin(), "material.hbMin");
        BigDecimal hbMax = requirePositive(material.getHbMax(), "material.hbMax");
        BigDecimal sigmaBMpa = requirePositive(material.getSigmaBMpa(), "material.sigmaBMpa");
        BigDecimal sigmaChMpa = requirePositive(material.getSigmaChMpa(), "material.sigmaChMpa");

        BigDecimal hbAverage = scale(hbMin.add(hbMax, MATH_CONTEXT).divide(TWO, MATH_CONTEXT));
        BigDecimal operatingCycles = speed.multiply(SIXTY, MATH_CONTEXT).multiply(lifeHours, MATH_CONTEXT);

        BigDecimal khl = clamp(
                bd(Math.pow(divide(CONTACT_BASE_CYCLES, operatingCycles).doubleValue(), 1.0d / 6.0d)),
                MIN_CONTACT_LIFE_FACTOR,
                MAX_LIFE_FACTOR
        );
        BigDecimal kfl = clamp(
                bd(Math.pow(divide(BENDING_BASE_CYCLES, operatingCycles).doubleValue(), 1.0d / 9.0d)),
                MIN_BENDING_LIFE_FACTOR,
                MAX_LIFE_FACTOR
        );

        BigDecimal baseAllowableContactStress = hbAverage.multiply(BigDecimal.valueOf(2.4), MATH_CONTEXT)
                .add(BigDecimal.valueOf(120), MATH_CONTEXT);
        BigDecimal baseAllowableBendingStress = min(
                sigmaBMpa.multiply(BigDecimal.valueOf(0.38), MATH_CONTEXT),
                sigmaChMpa.multiply(BigDecimal.valueOf(0.52), MATH_CONTEXT)
        );

        BigDecimal allowableContactStress = scale(baseAllowableContactStress.multiply(khl, MATH_CONTEXT));
        BigDecimal allowableBendingStress = scale(baseAllowableBendingStress.multiply(kfl, MATH_CONTEXT));

        BigDecimal initialDelta1Deg = scale(bd(Math.toDegrees(Math.atan(ONE.divide(requestedRatio, MATH_CONTEXT).doubleValue()))));
        BigDecimal reEstimate = scale(bd(RE_ESTIMATE_FACTOR.doubleValue() * Math.cbrt(divide(torque, requestedRatio).doubleValue())));
        BigDecimal de1Target = scale(TWO.multiply(reEstimate, MATH_CONTEXT).multiply(sinDeg(initialDelta1Deg), MATH_CONTEXT));

        Candidate candidate = selectCandidate(de1Target, requestedRatio);
        BigDecimal actualRatio = divide(BigDecimal.valueOf(candidate.z2()), BigDecimal.valueOf(candidate.z1()));
        BigDecimal ratioErrorPercent = scale(
                actualRatio.subtract(requestedRatio, MATH_CONTEXT)
                        .abs()
                        .multiply(HUNDRED, MATH_CONTEXT)
                        .divide(requestedRatio, MATH_CONTEXT)
        );

        BigDecimal delta1Deg = scale(bd(Math.toDegrees(Math.atan(ONE.divide(actualRatio, MATH_CONTEXT).doubleValue()))));
        BigDecimal delta2Deg = scale(NINETY.subtract(delta1Deg, MATH_CONTEXT));

        BigDecimal de1Calculated = scale(candidate.module().multiply(BigDecimal.valueOf(candidate.z1()), MATH_CONTEXT));
        BigDecimal de2Calculated = scale(candidate.module().multiply(BigDecimal.valueOf(candidate.z2()), MATH_CONTEXT));
        BigDecimal reCalculated = scale(divide(de1Calculated, TWO.multiply(sinDeg(delta1Deg), MATH_CONTEXT)));
        BigDecimal widthBMm = scale(BigDecimal.valueOf(Math.max(1L, Math.round(reCalculated.multiply(WIDTH_FACTOR, MATH_CONTEXT).doubleValue()))));
        BigDecimal diameterDm1Mm = scale(de1Calculated.subtract(widthBMm.multiply(sinDeg(delta1Deg), MATH_CONTEXT), MATH_CONTEXT));
        BigDecimal diameterDm2Mm = scale(de2Calculated.subtract(widthBMm.multiply(sinDeg(delta2Deg), MATH_CONTEXT), MATH_CONTEXT));

        BigDecimal shaft1FtN = scale(divide(TWO.multiply(torque, MATH_CONTEXT), diameterDm1Mm));
        BigDecimal shaft1FrN = scale(shaft1FtN.multiply(TANGENT_20, MATH_CONTEXT).multiply(cosDeg(delta1Deg), MATH_CONTEXT));
        BigDecimal shaft1FaN = scale(shaft1FtN.multiply(TANGENT_20, MATH_CONTEXT).multiply(sinDeg(delta1Deg), MATH_CONTEXT));
        BigDecimal shaft2FtN = shaft1FtN;
        BigDecimal shaft2FrN = shaft1FaN;
        BigDecimal shaft2FaN = shaft1FrN;

        BigDecimal sigmaHBase = divide(
                TWO.multiply(torque, MATH_CONTEXT)
                        .multiply(CONTACT_LOAD_FACTOR, MATH_CONTEXT)
                        .multiply(actualRatio.add(ONE, MATH_CONTEXT), MATH_CONTEXT),
                widthBMm.multiply(actualRatio, MATH_CONTEXT)
                        .multiply(diameterDm1Mm.pow(2, MATH_CONTEXT), MATH_CONTEXT)
        );
        BigDecimal sigmaHMpa = scale(
                CONTACT_MATERIAL_FACTOR.multiply(CONTACT_GEOMETRY_FACTOR, MATH_CONTEXT)
                        .multiply(sqrt(sigmaHBase), MATH_CONTEXT)
        );

        BigDecimal yf1 = toothFormFactor(candidate.z1(), true);
        BigDecimal yf2 = toothFormFactor(candidate.z2(), false);
        BigDecimal sigmaF1Mpa = scale(divide(
                TWO.multiply(torque, MATH_CONTEXT)
                        .multiply(BENDING_LOAD_FACTOR, MATH_CONTEXT)
                        .multiply(yf1, MATH_CONTEXT),
                widthBMm.multiply(diameterDm1Mm, MATH_CONTEXT).multiply(candidate.module(), MATH_CONTEXT)
        ));
        BigDecimal sigmaF2Mpa = scale(sigmaF1Mpa.multiply(yf2, MATH_CONTEXT).divide(yf1, MATH_CONTEXT));

        boolean contactStressPass = sigmaHMpa.compareTo(allowableContactStress) <= 0;
        boolean bendingStressPass = sigmaF1Mpa.compareTo(allowableBendingStress) <= 0
                && sigmaF2Mpa.compareTo(allowableBendingStress) <= 0;

        return new CalculationResult(
                allowableContactStress,
                allowableBendingStress,
                reCalculated,
                de1Calculated,
                scale(candidate.module()),
                candidate.z1(),
                candidate.z2(),
                scale(actualRatio),
                widthBMm,
                diameterDm1Mm,
                diameterDm2Mm,
                delta1Deg,
                delta2Deg,
                sigmaHMpa,
                sigmaF1Mpa,
                sigmaF2Mpa,
                contactStressPass,
                bendingStressPass,
                shaft1FtN,
                shaft1FrN,
                shaft1FaN,
                shaft2FtN,
                shaft2FrN,
                shaft2FaN,
                ratioErrorPercent,
                ratioErrorPercent.compareTo(BigDecimal.valueOf(4)) > 0
        );
    }

    private static Candidate selectCandidate(BigDecimal de1Target, BigDecimal requestedRatio) {
        return STANDARD_MODULES.stream()
                .map(module -> buildCandidate(module, de1Target, requestedRatio))
                .min(Comparator
                        .comparing(Candidate::fallback)
                        .thenComparing(Candidate::diameterGap)
                        .thenComparing(Candidate::ratioErrorPercent)
                        .thenComparing(Candidate::module))
                .orElseThrow(() -> new IllegalStateException("No standard module candidate available"));
    }

    private static Candidate buildCandidate(BigDecimal module, BigDecimal de1Target, BigDecimal requestedRatio) {
        int z1 = Math.max(20, (int) Math.round(divide(de1Target, module).doubleValue()));
        int z2 = Math.max(z1 + 1, (int) Math.round(requestedRatio.multiply(BigDecimal.valueOf(z1), MATH_CONTEXT).doubleValue()));
        BigDecimal actualRatio = divide(BigDecimal.valueOf(z2), BigDecimal.valueOf(z1));
        BigDecimal ratioErrorPercent = actualRatio.subtract(requestedRatio, MATH_CONTEXT)
                .abs()
                .multiply(HUNDRED, MATH_CONTEXT)
                .divide(requestedRatio, MATH_CONTEXT);
        BigDecimal diameterGap = module.multiply(BigDecimal.valueOf(z1), MATH_CONTEXT)
                .subtract(de1Target, MATH_CONTEXT)
                .abs();
        boolean fallback = ratioErrorPercent.compareTo(BigDecimal.valueOf(4)) > 0;
        return new Candidate(module, z1, z2, ratioErrorPercent, diameterGap, fallback);
    }

    private static BigDecimal toothFormFactor(int teethCount, boolean pinion) {
        double value = pinion
                ? Math.max(3.20, Math.min(4.00, 4.20 - (0.015 * teethCount)))
                : Math.max(3.20, Math.min(3.80, 3.90 - (0.003 * teethCount)));
        return scale(bd(value));
    }

    private static BigDecimal sinDeg(BigDecimal angleDeg) {
        return bd(Math.sin(Math.toRadians(angleDeg.doubleValue())));
    }

    private static BigDecimal cosDeg(BigDecimal angleDeg) {
        return bd(Math.cos(Math.toRadians(angleDeg.doubleValue())));
    }

    private static BigDecimal sqrt(BigDecimal value) {
        return bd(Math.sqrt(requirePositive(value, "sqrt.value").doubleValue()));
    }

    private static BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(max) > 0) {
            return max;
        }
        return scale(value);
    }

    private static BigDecimal min(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) <= 0 ? left : right;
    }

    private static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(requirePositive(divisor, "divisor"), MATH_CONTEXT);
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
        return value;
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }

    record CalculationResult(
            BigDecimal allowableContactStressMpa,
            BigDecimal allowableBendingStressMpa,
            BigDecimal reCalculated,
            BigDecimal de1Calculated,
            BigDecimal moduleMteSelected,
            int teethZ1,
            int teethZ2,
            BigDecimal actualRatioU2,
            BigDecimal widthBMm,
            BigDecimal diameterDm1Mm,
            BigDecimal diameterDm2Mm,
            BigDecimal coneAngleDelta1Deg,
            BigDecimal coneAngleDelta2Deg,
            BigDecimal sigmaHMpa,
            BigDecimal sigmaF1Mpa,
            BigDecimal sigmaF2Mpa,
            boolean contactStressPass,
            boolean bendingStressPass,
            BigDecimal shaft1FtN,
            BigDecimal shaft1FrN,
            BigDecimal shaft1FaN,
            BigDecimal shaft2FtN,
            BigDecimal shaft2FrN,
            BigDecimal shaft2FaN,
            BigDecimal ratioErrorPercent,
            boolean usedFallbackCandidate
    ) {
    }

    private record Candidate(
            BigDecimal module,
            int z1,
            int z2,
            BigDecimal ratioErrorPercent,
            BigDecimal diameterGap,
            boolean fallback
    ) {
    }
}
