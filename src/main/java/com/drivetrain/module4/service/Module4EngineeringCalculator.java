package com.drivetrain.module4.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

final class Module4EngineeringCalculator {

    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);
    private static final int SCALE = 6;
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal THREE = BigDecimal.valueOf(3);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final BigDecimal KA = new BigDecimal("49.5");
    private static final BigDecimal PSI_BA = new BigDecimal("0.25");
    private static final BigDecimal ZM = new BigDecimal("274");
    private static final BigDecimal ZH = new BigDecimal("1.764");
    private static final BigDecimal Y_BETA = BigDecimal.ONE;
    private static final BigDecimal KH_BETA = new BigDecimal("1.03");
    private static final BigDecimal KF_BETA = new BigDecimal("1.08");
    private static final BigDecimal KH_ALPHA = BigDecimal.ONE;
    private static final BigDecimal KF_ALPHA = BigDecimal.ONE;
    private static final BigDecimal KH_V = new BigDecimal("1.035");
    private static final BigDecimal KF_V = new BigDecimal("1.09");
    private static final BigDecimal YF1 = new BigDecimal("3.80");
    private static final BigDecimal YF2 = new BigDecimal("3.60");
    private static final BigDecimal TANGENT_20 = BigDecimal.valueOf(Math.tan(Math.toRadians(20)));

    private static final List<BigDecimal> STANDARD_MODULES = List.of(
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(1.25),
            BigDecimal.valueOf(1.5),
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

    private Module4EngineeringCalculator() {
    }

    static CalculationResult calculate(
            BigDecimal inputT2Nmm,
            BigDecimal inputN2Rpm,
            BigDecimal inputU3,
            BigDecimal allowableContactStressMpa,
            BigDecimal allowableBendingStressGear1Mpa,
            BigDecimal allowableBendingStressGear2Mpa
    ) {
        BigDecimal torque = requirePositive(inputT2Nmm, "inputT2Nmm");
        BigDecimal speed = requirePositive(inputN2Rpm, "inputN2Rpm");
        BigDecimal u3 = requirePositive(inputU3, "inputU3");
        BigDecimal sigmaHAllowable = requirePositive(allowableContactStressMpa, "allowableContactStressMpa");
        BigDecimal sigmaF1Allowable = requirePositive(allowableBendingStressGear1Mpa, "allowableBendingStressGear1Mpa");
        BigDecimal sigmaF2Allowable = requirePositive(allowableBendingStressGear2Mpa, "allowableBendingStressGear2Mpa");

        // Step 1 — center distance
        // aw = Ka * (u3 + 1) * cbrt((T2 * KHBeta) / (sigmaH^2 * u3 * psiBa))
        BigDecimal u3Plus1 = u3.add(BigDecimal.ONE, MATH_CONTEXT);
        BigDecimal numerator = torque.multiply(KH_BETA, MATH_CONTEXT);
        BigDecimal denominator = sigmaHAllowable.pow(2, MATH_CONTEXT)
                .multiply(u3, MATH_CONTEXT)
                .multiply(PSI_BA, MATH_CONTEXT);
        BigDecimal cbrtArg = divide(numerator, denominator);
        BigDecimal cbrtValue = bd(Math.cbrt(cbrtArg.doubleValue()));
        BigDecimal centerDistanceAw = scale(KA.multiply(u3Plus1, MATH_CONTEXT).multiply(cbrtValue, MATH_CONTEXT));

        // Step 2 — module and teeth selection
        BigDecimal moduleLow = centerDistanceAw.multiply(new BigDecimal("0.01"), MATH_CONTEXT);
        BigDecimal moduleHigh = centerDistanceAw.multiply(new BigDecimal("0.02"), MATH_CONTEXT);

        ModuleSelection selection = selectModule(centerDistanceAw, moduleLow, moduleHigh, u3);
        BigDecimal selectedModule = selection.module();
        boolean moduleOutOfRange = selection.outOfRange();

        int z1 = Math.round(TWO.multiply(centerDistanceAw, MATH_CONTEXT)
                .divide(selectedModule.multiply(u3Plus1, MATH_CONTEXT), MATH_CONTEXT)
                .floatValue());
        int z2 = Math.round(u3.multiply(BigDecimal.valueOf(z1), MATH_CONTEXT).floatValue());

        BigDecimal actualRatioU3 = scale(divide(BigDecimal.valueOf(z2), BigDecimal.valueOf(z1)));
        BigDecimal ratioErrorPercent = scale(
                actualRatioU3.subtract(u3, MATH_CONTEXT)
                        .abs()
                        .multiply(HUNDRED, MATH_CONTEXT)
                        .divide(u3, MATH_CONTEXT)
        );

        // Step 3 — geometry
        BigDecimal actualU3Plus1 = actualRatioU3.add(BigDecimal.ONE, MATH_CONTEXT);
        BigDecimal dw1 = scale(TWO.multiply(centerDistanceAw, MATH_CONTEXT).divide(actualU3Plus1, MATH_CONTEXT));
        BigDecimal dw2 = scale(dw1.multiply(actualRatioU3, MATH_CONTEXT));
        BigDecimal bw = scale(PSI_BA.multiply(centerDistanceAw, MATH_CONTEXT));

        // Step 4 — derived factors
        BigDecimal invZ1 = divide(BigDecimal.ONE, BigDecimal.valueOf(z1));
        BigDecimal invZ2 = divide(BigDecimal.ONE, BigDecimal.valueOf(z2));
        BigDecimal epsilonAlpha = scale(
                new BigDecimal("1.88").subtract(
                        new BigDecimal("3.2").multiply(
                                bd(Math.sqrt(invZ1.add(invZ2, MATH_CONTEXT).doubleValue())),
                                MATH_CONTEXT
                        ),
                        MATH_CONTEXT
                )
        );
        BigDecimal zEpsilon = scale(bd(Math.sqrt(
                FOUR.subtract(epsilonAlpha, MATH_CONTEXT).divide(THREE, MATH_CONTEXT).doubleValue()
        )));
        BigDecimal yEpsilon = scale(divide(BigDecimal.ONE, epsilonAlpha));

        BigDecimal kH = scale(KH_BETA.multiply(KH_ALPHA, MATH_CONTEXT).multiply(KH_V, MATH_CONTEXT));
        BigDecimal kF = scale(KF_BETA.multiply(KF_ALPHA, MATH_CONTEXT).multiply(KF_V, MATH_CONTEXT));

        // Step 5 — stress validation
        // sigmaH = ZM * ZH * zEpsilon * sqrt((2 * T2 * KH) / ((u3+1) * bw * u3 * dw1^2))
        BigDecimal sigmaHNumerator = TWO.multiply(torque, MATH_CONTEXT).multiply(kH, MATH_CONTEXT);
        BigDecimal sigmaHDenominator = actualU3Plus1
                .multiply(bw, MATH_CONTEXT)
                .multiply(actualRatioU3, MATH_CONTEXT)
                .multiply(dw1.pow(2, MATH_CONTEXT), MATH_CONTEXT);
        BigDecimal sigmaHSqrtArg = divide(sigmaHNumerator, sigmaHDenominator);
        BigDecimal sigmaH = scale(
                ZM.multiply(ZH, MATH_CONTEXT)
                        .multiply(zEpsilon, MATH_CONTEXT)
                        .multiply(bd(Math.sqrt(sigmaHSqrtArg.doubleValue())), MATH_CONTEXT)
        );

        // sigmaF1 = (2 * T2 * KF * YBeta * yEpsilon * YF1) / (bw * dw1 * m)
        BigDecimal sigmaF1Numerator = TWO.multiply(torque, MATH_CONTEXT)
                .multiply(kF, MATH_CONTEXT)
                .multiply(Y_BETA, MATH_CONTEXT)
                .multiply(yEpsilon, MATH_CONTEXT)
                .multiply(YF1, MATH_CONTEXT);
        BigDecimal sigmaF1Denominator = bw.multiply(dw1, MATH_CONTEXT).multiply(selectedModule, MATH_CONTEXT);
        BigDecimal sigmaF1 = scale(divide(sigmaF1Numerator, sigmaF1Denominator));

        // sigmaF2 = sigmaF1 * YF2 / YF1
        BigDecimal sigmaF2 = scale(sigmaF1.multiply(YF2, MATH_CONTEXT).divide(YF1, MATH_CONTEXT));

        boolean contactStressPass = sigmaH.compareTo(sigmaHAllowable) <= 0;
        boolean bendingStressGear1Pass = sigmaF1.compareTo(sigmaF1Allowable) <= 0;
        boolean bendingStressGear2Pass = sigmaF2.compareTo(sigmaF2Allowable) <= 0;

        // Step 6 — forces
        BigDecimal ft = scale(divide(TWO.multiply(torque, MATH_CONTEXT), dw1));
        BigDecimal fr = scale(ft.multiply(TANGENT_20, MATH_CONTEXT));
        BigDecimal fa = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);

        return new CalculationResult(
                centerDistanceAw,
                selectedModule,
                z1,
                z2,
                actualRatioU3,
                ratioErrorPercent,
                dw1,
                dw2,
                bw,
                epsilonAlpha,
                zEpsilon,
                yEpsilon,
                scale(YF1),
                scale(YF2),
                kH,
                kF,
                sigmaH,
                sigmaF1,
                sigmaF2,
                contactStressPass,
                bendingStressGear1Pass,
                bendingStressGear2Pass,
                ft,
                fr,
                fa,
                moduleOutOfRange
        );
    }

    private static ModuleSelection selectModule(
            BigDecimal centerDistanceAw,
            BigDecimal moduleLow,
            BigDecimal moduleHigh,
            BigDecimal u3
    ) {
        BigDecimal target = centerDistanceAw.multiply(new BigDecimal("0.02"), MATH_CONTEXT);
        BigDecimal bestInRange = null;
        BigDecimal closestToTarget = null;
        BigDecimal closestDistance = null;

        for (BigDecimal m : STANDARD_MODULES) {
            BigDecimal distToTarget = m.subtract(target, MATH_CONTEXT).abs();
            if (closestDistance == null || distToTarget.compareTo(closestDistance) < 0) {
                closestDistance = distToTarget;
                closestToTarget = m;
            }
            if (m.compareTo(moduleLow) >= 0 && m.compareTo(moduleHigh) <= 0) {
                if (bestInRange == null || m.compareTo(bestInRange) > 0) {
                    bestInRange = m;
                }
            }
        }

        if (bestInRange != null) {
            return new ModuleSelection(bestInRange, false);
        }
        return new ModuleSelection(closestToTarget, true);
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
            BigDecimal centerDistanceAwMm,
            BigDecimal moduleMSelected,
            int teethZ1,
            int teethZ2,
            BigDecimal actualRatioU3,
            BigDecimal ratioErrorPercent,
            BigDecimal diameterDw1Mm,
            BigDecimal diameterDw2Mm,
            BigDecimal widthBwMm,
            BigDecimal epsilonAlpha,
            BigDecimal zEpsilon,
            BigDecimal yEpsilon,
            BigDecimal yF1,
            BigDecimal yF2,
            BigDecimal loadFactorKh,
            BigDecimal loadFactorKf,
            BigDecimal sigmaHMpa,
            BigDecimal sigmaF1Mpa,
            BigDecimal sigmaF2Mpa,
            boolean contactStressPass,
            boolean bendingStressGear1Pass,
            boolean bendingStressGear2Pass,
            BigDecimal ftN,
            BigDecimal frN,
            BigDecimal faN,
            boolean moduleOutOfRange
    ) {
    }

    private record ModuleSelection(BigDecimal module, boolean outOfRange) {
    }
}
