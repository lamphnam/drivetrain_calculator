package com.drivetrain.module4.service;

import com.drivetrain.domain.entity.DesignCase;
import com.drivetrain.domain.entity.Module1Result;
import com.drivetrain.domain.entity.Module3Result;
import com.drivetrain.domain.entity.Module4Result;
import com.drivetrain.domain.entity.Module4ShaftForce;
import com.drivetrain.domain.entity.ShaftState;
import com.drivetrain.domain.enums.DesignCaseStatus;
import com.drivetrain.domain.enums.ShaftCode;
import com.drivetrain.domain.repository.DesignCaseRepository;
import com.drivetrain.domain.repository.Module1ResultRepository;
import com.drivetrain.domain.repository.Module3ResultRepository;
import com.drivetrain.domain.repository.Module4ResultRepository;
import com.drivetrain.module4.dto.Module4CalculationRequest;
import com.drivetrain.module4.dto.Module4CalculationResponse;
import com.drivetrain.module4.exception.InvalidModule4InputException;
import com.drivetrain.module4.exception.Module4DesignCaseNotFoundException;
import com.drivetrain.module4.exception.Module4PrerequisiteMissingException;
import com.drivetrain.module4.exception.Module4ResultNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Module4CalculationService {

    private static final int SCALE = 6;
    private static final BigDecimal DEFAULT_ALLOWABLE_CONTACT_STRESS = new BigDecimal("600");
    private static final BigDecimal DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR1 = new BigDecimal("260");
    private static final BigDecimal DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR2 = new BigDecimal("260");

    private final DesignCaseRepository designCaseRepository;
    private final Module1ResultRepository module1ResultRepository;
    private final Module3ResultRepository module3ResultRepository;
    private final Module4ResultRepository module4ResultRepository;

    @Transactional
    public Module4CalculationResponse calculate(Module4CalculationRequest request) {
        DesignCase designCase = designCaseRepository.findById(request.designCaseId())
                .orElseThrow(() -> new Module4DesignCaseNotFoundException(request.designCaseId()));
        Module1Result module1Result = module1ResultRepository.findDetailedByDesignCaseId(designCase.getId())
                .orElseThrow(() -> new Module4PrerequisiteMissingException(
                        "Module 1 result is required before calculating Module 4 for design case " + designCase.getId()
                ));
        ShaftState shaft2 = resolveRequiredShaftState(module1Result, ShaftCode.SHAFT_2);

        ResolvedInputs inputs = resolveInputs(request, module1Result, shaft2, designCase.getId());
        Module4EngineeringCalculator.CalculationResult calculated = calculateGeometry(inputs);
        List<String> calculationNotes = buildCalculationNotes(inputs, calculated);

        replaceExistingModule4Result(designCase);

        Module4Result module4Result = Module4Result.builder()
                .designCase(designCase)
                .inputT2Nmm(inputs.inputT2Nmm())
                .inputN2Rpm(inputs.inputN2Rpm())
                .inputU3(inputs.inputU3())
                .allowableContactStressMpa(inputs.allowableContactStressMpa())
                .allowableBendingStressGear1Mpa(inputs.allowableBendingStressGear1Mpa())
                .allowableBendingStressGear2Mpa(inputs.allowableBendingStressGear2Mpa())
                .centerDistanceAwMm(calculated.centerDistanceAwMm())
                .moduleMSelected(calculated.moduleMSelected())
                .teethZ1(calculated.teethZ1())
                .teethZ2(calculated.teethZ2())
                .actualRatioU3(calculated.actualRatioU3())
                .ratioErrorPercent(calculated.ratioErrorPercent())
                .diameterDw1Mm(calculated.diameterDw1Mm())
                .diameterDw2Mm(calculated.diameterDw2Mm())
                .widthBwMm(calculated.widthBwMm())
                .epsilonAlpha(calculated.epsilonAlpha())
                .zEpsilon(calculated.zEpsilon())
                .yEpsilon(calculated.yEpsilon())
                .yF1(calculated.yF1())
                .yF2(calculated.yF2())
                .loadFactorKh(calculated.loadFactorKh())
                .loadFactorKf(calculated.loadFactorKf())
                .sigmaHMpa(calculated.sigmaHMpa())
                .sigmaF1Mpa(calculated.sigmaF1Mpa())
                .sigmaF2Mpa(calculated.sigmaF2Mpa())
                .contactStressPass(calculated.contactStressPass())
                .bendingStressGear1Pass(calculated.bendingStressGear1Pass())
                .bendingStressGear2Pass(calculated.bendingStressGear2Pass())
                .calculationNote(serializeNotes(calculationNotes))
                .build();

        module4Result.addShaftForce(buildShaftForce(ShaftCode.SHAFT_2, calculated.ftN(), calculated.frN(), calculated.faN()));
        module4Result.addShaftForce(buildShaftForce(ShaftCode.SHAFT_3, calculated.ftN(), calculated.frN(), calculated.faN()));

        Module4Result savedResult = module4ResultRepository.save(module4Result);
        designCase.setModule4Result(savedResult);
        designCase.setStatus(DesignCaseStatus.MODULE4_COMPLETED);
        designCaseRepository.save(designCase);

        return mapToResponse(savedResult);
    }

    @Transactional(readOnly = true)
    public Module4CalculationResponse getCalculation(Long designCaseId) {
        Module4Result module4Result = module4ResultRepository.findDetailedByDesignCaseId(designCaseId)
                .orElseThrow(() -> new Module4ResultNotFoundException(designCaseId));
        return mapToResponse(module4Result);
    }

    private Module4EngineeringCalculator.CalculationResult calculateGeometry(ResolvedInputs inputs) {
        try {
            return Module4EngineeringCalculator.calculate(
                    inputs.inputT2Nmm(),
                    inputs.inputN2Rpm(),
                    inputs.inputU3(),
                    inputs.allowableContactStressMpa(),
                    inputs.allowableBendingStressGear1Mpa(),
                    inputs.allowableBendingStressGear2Mpa()
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidModule4InputException(exception.getMessage());
        }
    }

    private ResolvedInputs resolveInputs(
            Module4CalculationRequest request,
            Module1Result module1Result,
            ShaftState shaft2,
            Long designCaseId
    ) {
        BigDecimal inputT2Nmm = request.inputT2Nmm() != null
                ? scale(request.inputT2Nmm())
                : requirePositive(shaft2.getTorqueNmm(), "module1.shaft2.torqueNmm");
        BigDecimal inputN2Rpm = request.inputN2Rpm() != null
                ? scale(request.inputN2Rpm())
                : requirePositive(shaft2.getRpm(), "module1.shaft2.rpm");
        BigDecimal inputU3 = request.inputU3() != null
                ? scale(request.inputU3())
                : requirePositive(module1Result.getSpurGearRatioU3(), "module1.spurGearRatioU3");

        StressResolution stressResolution = resolveStresses(request, designCaseId);

        return new ResolvedInputs(
                inputT2Nmm,
                inputN2Rpm,
                inputU3,
                stressResolution.allowableContactStressMpa(),
                stressResolution.allowableBendingStressGear1Mpa(),
                stressResolution.allowableBendingStressGear2Mpa(),
                request.inputT2Nmm() != null ? ValueSource.REQUEST : ValueSource.MODULE1,
                request.inputN2Rpm() != null ? ValueSource.REQUEST : ValueSource.MODULE1,
                request.inputU3() != null ? ValueSource.REQUEST : ValueSource.MODULE1,
                stressResolution.source()
        );
    }

    private StressResolution resolveStresses(Module4CalculationRequest request, Long designCaseId) {
        if (request.allowableContactStressMpa() != null
                || request.allowableBendingStressGear1Mpa() != null
                || request.allowableBendingStressGear2Mpa() != null) {
            return new StressResolution(
                    scale(request.allowableContactStressMpa() != null
                            ? request.allowableContactStressMpa() : DEFAULT_ALLOWABLE_CONTACT_STRESS),
                    scale(request.allowableBendingStressGear1Mpa() != null
                            ? request.allowableBendingStressGear1Mpa() : DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR1),
                    scale(request.allowableBendingStressGear2Mpa() != null
                            ? request.allowableBendingStressGear2Mpa() : DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR2),
                    ValueSource.REQUEST
            );
        }

        return module3ResultRepository.findByDesignCaseId(designCaseId)
                .map(module3Result -> new StressResolution(
                        scale(module3Result.getAllowableContactStressMpa() != null
                                ? module3Result.getAllowableContactStressMpa() : DEFAULT_ALLOWABLE_CONTACT_STRESS),
                        scale(module3Result.getAllowableBendingStressMpa() != null
                                ? module3Result.getAllowableBendingStressMpa() : DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR1),
                        scale(module3Result.getAllowableBendingStressMpa() != null
                                ? module3Result.getAllowableBendingStressMpa() : DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR2),
                        ValueSource.MODULE3
                ))
                .orElse(new StressResolution(
                        scale(DEFAULT_ALLOWABLE_CONTACT_STRESS),
                        scale(DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR1),
                        scale(DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR2),
                        ValueSource.DEFAULT
                ));
    }

    private ShaftState resolveRequiredShaftState(Module1Result module1Result, ShaftCode shaftCode) {
        return module1Result.getShaftStates().stream()
                .filter(shaftState -> shaftState.getShaftCode() == shaftCode)
                .findFirst()
                .orElseThrow(() -> new Module4PrerequisiteMissingException(
                        "Module 1 result is missing shaft state " + shaftCode + " for design case "
                                + module1Result.getDesignCase().getId()
                ));
    }

    private Module4ShaftForce buildShaftForce(ShaftCode shaftCode, BigDecimal ftN, BigDecimal frN, BigDecimal faN) {
        return Module4ShaftForce.builder()
                .shaftCode(shaftCode)
                .ftN(scale(ftN))
                .frN(scale(frN))
                .faN(scale(faN))
                .build();
    }

    private void replaceExistingModule4Result(DesignCase designCase) {
        module4ResultRepository.findByDesignCaseId(designCase.getId())
                .ifPresent(existingResult -> {
                    designCase.setModule4Result(null);
                    module4ResultRepository.delete(existingResult);
                    module4ResultRepository.flush();
                });
    }

    private List<String> buildCalculationNotes(
            ResolvedInputs inputs,
            Module4EngineeringCalculator.CalculationResult calculated
    ) {
        List<String> notes = new ArrayList<>();
        notes.add(buildSourceNote("Input torque T2", inputs.t2Source(), "Module 1 shaft state SHAFT_2"));
        notes.add(buildSourceNote("Input speed n2", inputs.n2Source(), "Module 1 shaft state SHAFT_2"));
        notes.add(buildSourceNote("Input spur ratio U3", inputs.u3Source(), "stored Module 1 ratio U3"));

        switch (inputs.stressSource()) {
            case REQUEST -> notes.add("Allowable stresses were provided directly in the Module 4 request payload.");
            case MODULE3 -> notes.add("Allowable stresses were inherited from the Module 3 result for the same design case.");
            case DEFAULT -> notes.add("Allowable stresses used backend defaults (σH=600 MPa, σF1=260 MPa, σF2=260 MPa) because no request values or Module 3 result were available.");
            default -> { }
        }

        if (calculated.moduleOutOfRange()) {
            notes.add("No standard module fell within the valid range [0.01*aw, 0.02*aw], so the closest standard module to 0.02*aw was selected.");
        } else {
            notes.add("Selected module is the largest standard module within the valid range [0.01*aw, 0.02*aw].");
        }

        notes.add("Actual ratio error versus requested U3: " + calculated.ratioErrorPercent().toPlainString() + "%.");
        notes.add("Axial force Fa = 0 because straight spur gear teeth produce no axial thrust.");
        return notes;
    }

    private String buildSourceNote(String label, ValueSource source, String inheritedSourceLabel) {
        return switch (source) {
            case REQUEST -> label + " was overridden directly in the Module 4 request payload.";
            case MODULE1 -> label + " was inherited from " + inheritedSourceLabel + ".";
            case MODULE3 -> label + " was inherited from Module 3 result.";
            case DEFAULT -> label + " used the backend default value.";
        };
    }

    private Module4CalculationResponse mapToResponse(Module4Result module4Result) {
        DesignCase designCase = module4Result.getDesignCase();

        Module4CalculationResponse.ResultInfo resultInfo = new Module4CalculationResponse.ResultInfo(
                module4Result.getId(),
                module4Result.getCreatedAt(),
                module4Result.getUpdatedAt()
        );

        Module4CalculationResponse.CaseInfo caseInfo = new Module4CalculationResponse.CaseInfo(
                designCase.getId(),
                designCase.getCaseCode(),
                designCase.getCaseName(),
                designCase.getStatus().name()
        );

        Module4CalculationResponse.InputSummary inputSummary = new Module4CalculationResponse.InputSummary(
                module4Result.getInputT2Nmm(),
                module4Result.getInputN2Rpm(),
                module4Result.getInputU3(),
                module4Result.getAllowableContactStressMpa(),
                module4Result.getAllowableBendingStressGear1Mpa(),
                module4Result.getAllowableBendingStressGear2Mpa()
        );

        Module4CalculationResponse.SpurGearGeometry spurGearGeometry = new Module4CalculationResponse.SpurGearGeometry(
                module4Result.getCenterDistanceAwMm(),
                module4Result.getModuleMSelected(),
                module4Result.getTeethZ1(),
                module4Result.getTeethZ2(),
                module4Result.getActualRatioU3(),
                module4Result.getRatioErrorPercent(),
                module4Result.getDiameterDw1Mm(),
                module4Result.getDiameterDw2Mm(),
                module4Result.getWidthBwMm()
        );

        Module4CalculationResponse.DerivedFactors derivedFactors = new Module4CalculationResponse.DerivedFactors(
                module4Result.getEpsilonAlpha(),
                module4Result.getZEpsilon(),
                module4Result.getYEpsilon(),
                module4Result.getYF1(),
                module4Result.getYF2(),
                module4Result.getLoadFactorKh(),
                module4Result.getLoadFactorKf()
        );

        Module4CalculationResponse.StressCheck stressCheck = new Module4CalculationResponse.StressCheck(
                module4Result.getSigmaHMpa(),
                module4Result.getSigmaF1Mpa(),
                module4Result.getSigmaF2Mpa(),
                module4Result.isContactStressPass(),
                module4Result.isBendingStressGear1Pass(),
                module4Result.isBendingStressGear2Pass()
        );

        List<Module4CalculationResponse.ShaftForceSummary> shaftForces = module4Result.getShaftForces().stream()
                .sorted(Comparator.comparingInt(force -> switch (force.getShaftCode()) {
                    case SHAFT_2 -> 1;
                    case SHAFT_3 -> 2;
                    default -> 99;
                }))
                .map(force -> new Module4CalculationResponse.ShaftForceSummary(
                        force.getShaftCode(),
                        resolveShaftLabel(force.getShaftCode()),
                        force.getFtN(),
                        force.getFrN(),
                        force.getFaN()
                ))
                .toList();

        return new Module4CalculationResponse(
                resultInfo,
                caseInfo,
                inputSummary,
                spurGearGeometry,
                derivedFactors,
                stressCheck,
                shaftForces,
                deserializeNotes(module4Result.getCalculationNote())
        );
    }

    private String resolveShaftLabel(ShaftCode shaftCode) {
        return switch (shaftCode) {
            case SHAFT_2 -> "Shaft 2";
            case SHAFT_3 -> "Shaft 3";
            case MOTOR -> "Motor Shaft";
            case SHAFT_1 -> "Shaft 1";
            case DRUM_SHAFT -> "Output Drum Shaft";
        };
    }

    private String serializeNotes(List<String> calculationNotes) {
        return String.join(System.lineSeparator(), calculationNotes);
    }

    private List<String> deserializeNotes(String calculationNote) {
        if (calculationNote == null || calculationNote.isBlank()) {
            return List.of();
        }
        return calculationNote.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new Module4PrerequisiteMissingException(fieldName + " must be greater than zero");
        }
        return scale(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private enum ValueSource {
        REQUEST,
        MODULE1,
        MODULE3,
        DEFAULT
    }

    private record ResolvedInputs(
            BigDecimal inputT2Nmm,
            BigDecimal inputN2Rpm,
            BigDecimal inputU3,
            BigDecimal allowableContactStressMpa,
            BigDecimal allowableBendingStressGear1Mpa,
            BigDecimal allowableBendingStressGear2Mpa,
            ValueSource t2Source,
            ValueSource n2Source,
            ValueSource u3Source,
            ValueSource stressSource
    ) {
    }

    private record StressResolution(
            BigDecimal allowableContactStressMpa,
            BigDecimal allowableBendingStressGear1Mpa,
            BigDecimal allowableBendingStressGear2Mpa,
            ValueSource source
    ) {
    }
}
