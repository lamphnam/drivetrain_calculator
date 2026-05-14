package com.drivetrain.module3.service;

import com.drivetrain.domain.entity.DesignCase;
import com.drivetrain.domain.entity.GearMaterial;
import com.drivetrain.domain.entity.Module1Result;
import com.drivetrain.domain.entity.Module3Result;
import com.drivetrain.domain.entity.Module3ShaftForce;
import com.drivetrain.domain.entity.ShaftState;
import com.drivetrain.domain.enums.DesignCaseStatus;
import com.drivetrain.domain.enums.ShaftCode;
import com.drivetrain.domain.repository.DesignCaseRepository;
import com.drivetrain.domain.repository.GearMaterialRepository;
import com.drivetrain.domain.repository.Module1ResultRepository;
import com.drivetrain.domain.repository.Module3ResultRepository;
import com.drivetrain.domain.repository.Module4ResultRepository;
import com.drivetrain.module3.dto.GearMaterialReferenceResponse;
import com.drivetrain.module3.dto.Module3CalculationRequest;
import com.drivetrain.module3.dto.Module3CalculationResponse;
import com.drivetrain.module3.exception.GearMaterialNotFoundException;
import com.drivetrain.module3.exception.InvalidModule3InputException;
import com.drivetrain.module3.exception.Module3DesignCaseNotFoundException;
import com.drivetrain.module3.exception.Module3PrerequisiteMissingException;
import com.drivetrain.module3.exception.Module3ResultNotFoundException;
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
public class Module3CalculationService {

    private static final int SCALE = 6;
    private static final BigDecimal DEFAULT_SERVICE_LIFE_HOURS = new BigDecimal("43200");

    private final DesignCaseRepository designCaseRepository;
    private final Module1ResultRepository module1ResultRepository;
    private final GearMaterialRepository gearMaterialRepository;
    private final Module3ResultRepository module3ResultRepository;
    private final Module4ResultRepository module4ResultRepository;

    @Transactional
    public Module3CalculationResponse calculate(Module3CalculationRequest request) {
        DesignCase designCase = designCaseRepository.findById(request.designCaseId())
                .orElseThrow(() -> new Module3DesignCaseNotFoundException(request.designCaseId()));
        Module1Result module1Result = module1ResultRepository.findDetailedByDesignCaseId(designCase.getId())
                .orElseThrow(() -> new Module3PrerequisiteMissingException(
                        "Module 1 result is required before calculating Module 3 for design case " + designCase.getId()
                ));
        GearMaterial material = gearMaterialRepository.findById(request.materialId())
                .orElseThrow(() -> new GearMaterialNotFoundException(request.materialId()));
        ShaftState shaft1 = resolveRequiredShaftState(module1Result, ShaftCode.SHAFT_1);

        ResolvedInputs inputs = resolveInputs(request, designCase, module1Result, shaft1);
        Module3EngineeringCalculator.CalculationResult calculated = calculateGeometry(inputs, material);
        List<String> calculationNotes = buildCalculationNotes(inputs, material, calculated);

        replaceExistingModule4Result(designCase);
        replaceExistingModule3Result(designCase);

        Module3Result module3Result = Module3Result.builder()
                .designCase(designCase)
                .material(material)
                .inputT1Nmm(inputs.inputT1Nmm())
                .inputN1Rpm(inputs.inputN1Rpm())
                .inputU2(inputs.inputU2())
                .serviceLifeHours(inputs.serviceLifeHours())
                .allowableContactStressMpa(calculated.allowableContactStressMpa())
                .allowableBendingStressMpa(calculated.allowableBendingStressMpa())
                .reCalculated(calculated.reCalculated())
                .de1Calculated(calculated.de1Calculated())
                .moduleMteSelected(calculated.moduleMteSelected())
                .teethZ1(calculated.teethZ1())
                .teethZ2(calculated.teethZ2())
                .actualRatioU2(calculated.actualRatioU2())
                .widthBMm(calculated.widthBMm())
                .diameterDm1Mm(calculated.diameterDm1Mm())
                .diameterDm2Mm(calculated.diameterDm2Mm())
                .coneAngleDelta1Deg(calculated.coneAngleDelta1Deg())
                .coneAngleDelta2Deg(calculated.coneAngleDelta2Deg())
                .sigmaHMpa(calculated.sigmaHMpa())
                .sigmaF1Mpa(calculated.sigmaF1Mpa())
                .sigmaF2Mpa(calculated.sigmaF2Mpa())
                .contactStressPass(calculated.contactStressPass())
                .bendingStressPass(calculated.bendingStressPass())
                .calculationNote(serializeNotes(calculationNotes))
                .build();

        module3Result.addShaftForce(buildShaftForce(
                ShaftCode.SHAFT_1,
                calculated.shaft1FtN(),
                calculated.shaft1FrN(),
                calculated.shaft1FaN()
        ));
        module3Result.addShaftForce(buildShaftForce(
                ShaftCode.SHAFT_2,
                calculated.shaft2FtN(),
                calculated.shaft2FrN(),
                calculated.shaft2FaN()
        ));

        Module3Result savedResult = module3ResultRepository.save(module3Result);
        designCase.setServiceLifeHours(inputs.serviceLifeHours());
        designCase.setModule3Result(savedResult);
        designCase.setStatus(DesignCaseStatus.MODULE3_COMPLETED);
        designCaseRepository.save(designCase);

        return mapToResponse(savedResult);
    }

    @Transactional(readOnly = true)
    public List<GearMaterialReferenceResponse> getMaterials() {
        return gearMaterialRepository.findAllByOrderByMaterialCodeAsc().stream()
                .map(this::mapMaterialReference)
                .toList();
    }

    @Transactional(readOnly = true)
    public Module3CalculationResponse getCalculation(Long designCaseId) {
        Module3Result module3Result = module3ResultRepository.findDetailedByDesignCaseId(designCaseId)
                .orElseThrow(() -> new Module3ResultNotFoundException(designCaseId));
        return mapToResponse(module3Result);
    }

    private Module3EngineeringCalculator.CalculationResult calculateGeometry(
            ResolvedInputs inputs,
            GearMaterial material
    ) {
        try {
            return Module3EngineeringCalculator.calculate(
                    inputs.inputT1Nmm(),
                    inputs.inputN1Rpm(),
                    inputs.inputU2(),
                    inputs.serviceLifeHours(),
                    material
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidModule3InputException(exception.getMessage());
        }
    }

    private ResolvedInputs resolveInputs(
            Module3CalculationRequest request,
            DesignCase designCase,
            Module1Result module1Result,
            ShaftState shaft1
    ) {
        BigDecimal inputT1Nmm = request.inputT1Nmm() != null
                ? scale(request.inputT1Nmm())
                : requirePositive(shaft1.getTorqueNmm(), "module1.shaft1.torqueNmm");
        BigDecimal inputN1Rpm = request.inputN1Rpm() != null
                ? scale(request.inputN1Rpm())
                : requirePositive(shaft1.getRpm(), "module1.shaft1.rpm");
        BigDecimal inputU2 = request.inputU2() != null
                ? scale(request.inputU2())
                : requirePositive(module1Result.getBevelGearRatioU2(), "module1.bevelGearRatioU2");

        ValueSource serviceLifeSource;
        BigDecimal serviceLifeHours;
        if (request.serviceLifeHours() != null) {
            serviceLifeHours = scale(request.serviceLifeHours());
            serviceLifeSource = ValueSource.REQUEST;
        } else if (designCase.getServiceLifeHours() != null && designCase.getServiceLifeHours().signum() > 0) {
            serviceLifeHours = scale(designCase.getServiceLifeHours());
            serviceLifeSource = ValueSource.DESIGN_CASE;
        } else {
            serviceLifeHours = scale(DEFAULT_SERVICE_LIFE_HOURS);
            serviceLifeSource = ValueSource.DEFAULT;
        }

        return new ResolvedInputs(
                inputT1Nmm,
                inputN1Rpm,
                inputU2,
                serviceLifeHours,
                request.inputT1Nmm() != null ? ValueSource.REQUEST : ValueSource.MODULE1,
                request.inputN1Rpm() != null ? ValueSource.REQUEST : ValueSource.MODULE1,
                request.inputU2() != null ? ValueSource.REQUEST : ValueSource.MODULE1,
                serviceLifeSource
        );
    }

    private ShaftState resolveRequiredShaftState(Module1Result module1Result, ShaftCode shaftCode) {
        return module1Result.getShaftStates().stream()
                .filter(shaftState -> shaftState.getShaftCode() == shaftCode)
                .findFirst()
                .orElseThrow(() -> new Module3PrerequisiteMissingException(
                        "Module 1 result is missing shaft state " + shaftCode + " for design case "
                                + module1Result.getDesignCase().getId()
                ));
    }

    private Module3ShaftForce buildShaftForce(ShaftCode shaftCode, BigDecimal ftN, BigDecimal frN, BigDecimal faN) {
        return Module3ShaftForce.builder()
                .shaftCode(shaftCode)
                .ftN(scale(ftN))
                .frN(scale(frN))
                .faN(scale(faN))
                .build();
    }

    private void replaceExistingModule3Result(DesignCase designCase) {
        module3ResultRepository.findByDesignCaseId(designCase.getId())
                .ifPresent(existingResult -> {
                    designCase.setModule3Result(null);
                    module3ResultRepository.delete(existingResult);
                    module3ResultRepository.flush();
                });
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
            GearMaterial material,
            Module3EngineeringCalculator.CalculationResult calculated
    ) {
        List<String> notes = new ArrayList<>();
        notes.add(buildSourceNote("Input torque T1", inputs.t1Source(), "Module 1 shaft state SHAFT_1"));
        notes.add(buildSourceNote("Input speed n1", inputs.n1Source(), "Module 1 shaft state SHAFT_1"));
        notes.add(buildSourceNote("Input bevel ratio U2", inputs.u2Source(), "stored Module 1 ratio U2"));

        if (inputs.serviceLifeSource() == ValueSource.REQUEST) {
            notes.add("Service life hours Lh were taken from the Module 3 request payload.");
        } else if (inputs.serviceLifeSource() == ValueSource.DESIGN_CASE) {
            notes.add("Service life hours Lh were reused from the saved design case.");
        } else {
            notes.add("Service life hours Lh defaulted to 43200 h because no explicit value was saved yet.");
        }

        notes.add("Material " + material.getMaterialCode() + " was used to derive allowable contact and bending stresses.");
        notes.add("Allowable stress evaluation currently uses a seeded material-strength model with simplified life-factor and tooth-form approximations pending finalized handbook/table lookups.");

        if (inputs.u2Source() == ValueSource.MODULE1) {
            notes.add("When U2 is inherited from Module 1 it may still reflect the temporary placeholder split until a finalized upstream gearbox ratio allocation is available.");
        }

        if (calculated.usedFallbackCandidate()) {
            notes.add("No standard-module tooth combination stayed within the 4% ratio target, so the closest available candidate was kept.");
        } else {
            notes.add("Selected module and tooth counts keep the actual bevel ratio within the 4% target of the requested ratio.");
        }

        notes.add("Actual ratio error versus requested U2: " + calculated.ratioErrorPercent().toPlainString() + "%.");
        return notes;
    }

    private String buildSourceNote(String label, ValueSource source, String inheritedSourceLabel) {
        return switch (source) {
            case REQUEST -> label + " was overridden directly in the Module 3 request payload.";
            case MODULE1 -> label + " was inherited from " + inheritedSourceLabel + ".";
            case DESIGN_CASE -> label + " was reused from the saved design case.";
            case DEFAULT -> label + " used the backend default value for this product phase.";
        };
    }

    private GearMaterialReferenceResponse mapMaterialReference(GearMaterial material) {
        return new GearMaterialReferenceResponse(
                material.getId(),
                material.getMaterialCode(),
                material.getMaterialName(),
                material.getHeatTreatment(),
                material.getHbMin(),
                material.getHbMax(),
                material.getSigmaBMpa(),
                material.getSigmaChMpa()
        );
    }

    private Module3CalculationResponse mapToResponse(Module3Result module3Result) {
        DesignCase designCase = module3Result.getDesignCase();
        GearMaterial material = module3Result.getMaterial();

        Module3CalculationResponse.ResultInfo resultInfo = new Module3CalculationResponse.ResultInfo(
                module3Result.getId(),
                module3Result.getCreatedAt(),
                module3Result.getUpdatedAt()
        );

        Module3CalculationResponse.CaseInfo caseInfo = new Module3CalculationResponse.CaseInfo(
                designCase.getId(),
                designCase.getCaseCode(),
                designCase.getCaseName(),
                designCase.getStatus().name()
        );

        Module3CalculationResponse.InputSummary inputSummary = new Module3CalculationResponse.InputSummary(
                module3Result.getInputT1Nmm(),
                module3Result.getInputN1Rpm(),
                module3Result.getInputU2(),
                module3Result.getServiceLifeHours()
        );

        Module3CalculationResponse.MaterialSummary materialSummary = new Module3CalculationResponse.MaterialSummary(
                material.getId(),
                material.getMaterialCode(),
                material.getMaterialName(),
                material.getHeatTreatment(),
                material.getHbMin(),
                material.getHbMax(),
                material.getSigmaBMpa(),
                material.getSigmaChMpa()
        );

        Module3CalculationResponse.AllowableStressSummary allowableStressSummary =
                new Module3CalculationResponse.AllowableStressSummary(
                        module3Result.getAllowableContactStressMpa(),
                        module3Result.getAllowableBendingStressMpa()
                );

        Module3CalculationResponse.GearGeometrySummary gearGeometrySummary =
                new Module3CalculationResponse.GearGeometrySummary(
                        module3Result.getReCalculated(),
                        module3Result.getDe1Calculated(),
                        module3Result.getModuleMteSelected(),
                        module3Result.getTeethZ1(),
                        module3Result.getTeethZ2(),
                        module3Result.getActualRatioU2(),
                        module3Result.getWidthBMm(),
                        module3Result.getDiameterDm1Mm(),
                        module3Result.getDiameterDm2Mm(),
                        module3Result.getConeAngleDelta1Deg(),
                        module3Result.getConeAngleDelta2Deg()
                );

        Module3CalculationResponse.StressCheckSummary stressCheckSummary =
                new Module3CalculationResponse.StressCheckSummary(
                        module3Result.getSigmaHMpa(),
                        module3Result.getSigmaF1Mpa(),
                        module3Result.getSigmaF2Mpa(),
                        module3Result.isContactStressPass(),
                        module3Result.isBendingStressPass()
                );

        List<Module3CalculationResponse.ShaftForceSummary> shaftForces = module3Result.getShaftForces().stream()
                .sorted(Comparator.comparingInt(force -> switch (force.getShaftCode()) {
                    case SHAFT_1 -> 1;
                    case SHAFT_2 -> 2;
                    default -> 99;
                }))
                .map(force -> new Module3CalculationResponse.ShaftForceSummary(
                        force.getShaftCode(),
                        resolveShaftLabel(force.getShaftCode()),
                        force.getFtN(),
                        force.getFrN(),
                        force.getFaN()
                ))
                .toList();

        return new Module3CalculationResponse(
                resultInfo,
                caseInfo,
                inputSummary,
                materialSummary,
                allowableStressSummary,
                gearGeometrySummary,
                stressCheckSummary,
                shaftForces,
                deserializeNotes(module3Result.getCalculationNote())
        );
    }

    private String resolveShaftLabel(ShaftCode shaftCode) {
        return switch (shaftCode) {
            case SHAFT_1 -> "Shaft 1";
            case SHAFT_2 -> "Shaft 2";
            case MOTOR -> "Motor Shaft";
            case SHAFT_3 -> "Shaft 3";
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
            throw new Module3PrerequisiteMissingException(fieldName + " must be greater than zero");
        }
        return scale(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private enum ValueSource {
        REQUEST,
        MODULE1,
        DESIGN_CASE,
        DEFAULT
    }

    private record ResolvedInputs(
            BigDecimal inputT1Nmm,
            BigDecimal inputN1Rpm,
            BigDecimal inputU2,
            BigDecimal serviceLifeHours,
            ValueSource t1Source,
            ValueSource n1Source,
            ValueSource u2Source,
            ValueSource serviceLifeSource
    ) {
    }
}
