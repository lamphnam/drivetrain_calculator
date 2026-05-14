package com.drivetrain.module1.service;

import com.drivetrain.domain.entity.DesignCase;
import com.drivetrain.domain.entity.DesignConstantSet;
import com.drivetrain.domain.entity.Module1Result;
import com.drivetrain.domain.entity.Motor;
import com.drivetrain.domain.entity.ShaftState;
import com.drivetrain.domain.enums.DesignCaseStatus;
import com.drivetrain.domain.enums.ShaftCode;
import com.drivetrain.domain.repository.DesignCaseRepository;
import com.drivetrain.domain.repository.DesignConstantSetRepository;
import com.drivetrain.domain.repository.Module1ResultRepository;
import com.drivetrain.domain.repository.Module3ResultRepository;
import com.drivetrain.domain.repository.Module4ResultRepository;
import com.drivetrain.domain.repository.MotorRepository;
import com.drivetrain.module1.dto.Module1CalculationHistoryItemResponse;
import com.drivetrain.module1.dto.Module1ReferenceValuesResponse;
import com.drivetrain.module1.dto.Module1CalculationRequest;
import com.drivetrain.module1.dto.Module1CalculationResponse;
import com.drivetrain.module1.exception.DesignCaseNotFoundException;
import com.drivetrain.module1.exception.InvalidModule1InputException;
import com.drivetrain.module1.exception.MissingConstantSetException;
import com.drivetrain.module1.exception.NoSuitableMotorFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class Module1CalculationService {

    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);
    private static final int SCALE = 6;
    private static final BigDecimal TORQUE_CONSTANT = new BigDecimal("9550000");
    private static final BigDecimal DEFAULT_BEVEL_GEAR_RATIO_U2 = new BigDecimal("3.14");
    private static final String MODULE_LABEL = "Module 1";

    private final DesignCaseRepository designCaseRepository;
    private final DesignConstantSetRepository designConstantSetRepository;
    private final MotorRepository motorRepository;
    private final Module1ResultRepository module1ResultRepository;
    private final Module3ResultRepository module3ResultRepository;
    private final Module4ResultRepository module4ResultRepository;

    @Transactional
    public Module1CalculationResponse calculate(Module1CalculationRequest request) {
        BigDecimal requiredOutputPower = requirePositive(request.requiredPowerKw(), "request.requiredPowerKw");
        BigDecimal requiredOutputRpm = requirePositive(request.requiredOutputRpm(), "request.requiredOutputRpm");
        DesignConstantSet constantSet = resolveConstantSet(request.constantSetId());
        DesignCase designCase = createOrUpdateDesignCase(request, constantSet, requiredOutputPower, requiredOutputRpm);

        BigDecimal totalEfficiency = calculateTotalEfficiency(constantSet);
        BigDecimal requiredMotorPowerKw = calculateRequiredMotorPower(requiredOutputPower, totalEfficiency);
        BigDecimal beltRatioU1 = requirePositive(constantSet.getDefaultBeltRatioU1(), "constantSet.defaultBeltRatioU1");
        BigDecimal preliminaryGearboxRatioUh = requirePositive(
                constantSet.getDefaultGearboxRatioUh(),
                "constantSet.defaultGearboxRatioUh"
        );
        BigDecimal preliminaryMotorRpmNsb = calculatePreliminaryMotorRpm(requiredOutputRpm, beltRatioU1, preliminaryGearboxRatioUh);

        Motor selectedMotor = selectMotor(requiredMotorPowerKw, preliminaryMotorRpmNsb);
        BigDecimal totalTransmissionRatioU = scale(divide(selectedMotor.getRatedRpm(), requiredOutputRpm));
        BigDecimal gearboxTransmissionRatioUh = scale(divide(totalTransmissionRatioU, beltRatioU1));
        // Temporary placeholder until Module 3 provides the real bevel-gear ratio split.
        BigDecimal bevelGearRatioU2 = scale(DEFAULT_BEVEL_GEAR_RATIO_U2);
        BigDecimal spurGearRatioU3 = scale(divide(gearboxTransmissionRatioUh, bevelGearRatioU2));
        List<String> calculationNotes = buildCalculationNotes(constantSet);

        replaceExistingModule1Result(designCase);
        replaceExistingModule4Result(designCase);
        replaceExistingModule3Result(designCase);

        Module1Result module1Result = Module1Result.builder()
                .designCase(designCase)
                .selectedMotor(selectedMotor)
                .totalEfficiency(totalEfficiency)
                .requiredMotorPowerKw(requiredMotorPowerKw)
                .preliminaryMotorRpmNsb(preliminaryMotorRpmNsb)
                .totalTransmissionRatioU(totalTransmissionRatioU)
                .beltRatioU1(beltRatioU1)
                .gearboxTransmissionRatioUh(gearboxTransmissionRatioUh)
                .bevelGearRatioU2(bevelGearRatioU2)
                .spurGearRatioU3(spurGearRatioU3)
                .calculationNote(serializeNotes(calculationNotes))
                .build();

        buildShaftStates(selectedMotor, constantSet, beltRatioU1, bevelGearRatioU2, spurGearRatioU3)
                .forEach(module1Result::addShaftState);

        Module1Result savedResult = module1ResultRepository.save(module1Result);
        designCase.setModule1Result(savedResult);
        designCase.setStatus(DesignCaseStatus.MODULE1_COMPLETED);
        designCaseRepository.save(designCase);

        return mapToResponse(savedResult, motorRepository.countByIsActiveTrue());
    }

    @Transactional(readOnly = true)
    public Module1ReferenceValuesResponse getReferenceValues(Long constantSetId) {
        DesignConstantSet constantSet = resolveConstantSet(constantSetId);
        return new Module1ReferenceValuesResponse(
                constantSet.getId(),
                constantSet.getSetCode(),
                constantSet.getSetName(),
                motorRepository.countByIsActiveTrue(),
                constantSet.getDefaultBeltRatioU1(),
                constantSet.getDefaultGearboxRatioUh(),
                calculateTotalEfficiency(constantSet)
        );
    }

    @Transactional(readOnly = true)
    public List<Module1CalculationHistoryItemResponse> getHistory() {
        return module1ResultRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::mapToHistoryItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public Module1CalculationResponse getCalculation(Long designCaseId) {
        Module1Result module1Result = module1ResultRepository.findDetailedByDesignCaseId(designCaseId)
                .orElseThrow(() -> new DesignCaseNotFoundException(designCaseId));

        return mapToResponse(module1Result, motorRepository.countByIsActiveTrue());
    }

    private DesignConstantSet resolveConstantSet(Long constantSetId) {
        if (constantSetId != null) {
            return designConstantSetRepository.findById(constantSetId)
                    .orElseThrow(() -> new MissingConstantSetException(constantSetId));
        }

        return designConstantSetRepository.findFirstByIsActiveTrueOrderByIdAsc()
                .orElseThrow(() -> new MissingConstantSetException("No active default constant set found"));
    }

    private DesignCase createOrUpdateDesignCase(
            Module1CalculationRequest request,
            DesignConstantSet constantSet,
            BigDecimal requiredPowerKw,
            BigDecimal requiredOutputRpm
    ) {
        String normalizedCaseCode = normalizeText(request.caseCode());
        DesignCase designCase = normalizedCaseCode == null
                ? DesignCase.builder().build()
                : designCaseRepository.findByCaseCode(normalizedCaseCode)
                .orElseGet(DesignCase::new);

        if (designCase.getCaseCode() == null) {
            designCase.setCaseCode(normalizedCaseCode != null ? normalizedCaseCode : generateCaseCode());
        }

        designCase.setCaseName(resolveCaseName(request.caseName(), designCase));
        designCase.setRequiredPowerKw(scale(requiredPowerKw));
        designCase.setRequiredOutputRpm(scale(requiredOutputRpm));
        designCase.setConstantSet(constantSet);
        designCase.setStatus(DesignCaseStatus.DRAFT);

        return designCaseRepository.save(designCase);
    }

    private String resolveCaseName(String requestedCaseName, DesignCase designCase) {
        String normalizedCaseName = normalizeText(requestedCaseName);
        if (normalizedCaseName != null) {
            return normalizedCaseName;
        }
        if (normalizeText(designCase.getCaseName()) != null) {
            return designCase.getCaseName().trim();
        }
        return designCase.getCaseCode();
    }

    private String generateCaseCode() {
        String caseCode;
        do {
            caseCode = "MODULE1-" + UUID.randomUUID().toString().replace("-", "");
        } while (designCaseRepository.existsByCaseCode(caseCode));
        return caseCode;
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal calculateTotalEfficiency(DesignConstantSet constantSet) {
        BigDecimal etaKn = requirePositive(constantSet.getEtaKn(), "constantSet.etaKn");
        BigDecimal etaD = requirePositive(constantSet.getEtaD(), "constantSet.etaD");
        BigDecimal etaBrc = requirePositive(constantSet.getEtaBrc(), "constantSet.etaBrc");
        BigDecimal etaBrt = requirePositive(constantSet.getEtaBrt(), "constantSet.etaBrt");
        BigDecimal etaOl = requirePositive(constantSet.getEtaOl(), "constantSet.etaOl");

        return scale(
                etaKn.multiply(etaD, MATH_CONTEXT)
                        .multiply(etaBrc, MATH_CONTEXT)
                        .multiply(etaBrt, MATH_CONTEXT)
                        .multiply(etaOl.pow(3, MATH_CONTEXT), MATH_CONTEXT)
        );
    }

    private BigDecimal calculateRequiredMotorPower(BigDecimal outputPowerKw, BigDecimal totalEfficiency) {
        return scale(divide(outputPowerKw, totalEfficiency));
    }

    private BigDecimal calculatePreliminaryMotorRpm(
            BigDecimal outputRpm,
            BigDecimal beltRatioU1,
            BigDecimal preliminaryGearboxRatioUh
    ) {
        return scale(
                outputRpm.multiply(beltRatioU1, MATH_CONTEXT)
                        .multiply(preliminaryGearboxRatioUh, MATH_CONTEXT)
        );
    }

    private Motor selectMotor(BigDecimal requiredMotorPowerKw, BigDecimal preliminaryMotorRpmNsb) {
        List<Motor> candidates = motorRepository
                .findByIsActiveTrueAndRatedPowerKwGreaterThanEqualOrderByRatedRpmAsc(requiredMotorPowerKw);

        Comparator<Motor> closestRpmComparator = Comparator
                .comparing(
                        (Motor motor) -> motor.getRatedRpm()
                                .subtract(preliminaryMotorRpmNsb, MATH_CONTEXT)
                                .abs(),
                        BigDecimal::compareTo
                )
                .thenComparing(Motor::getRatedPowerKw)
                .thenComparing(Motor::getRatedRpm);

        return candidates.stream()
                .min(closestRpmComparator)
                .orElseThrow(() -> new NoSuitableMotorFoundException(requiredMotorPowerKw));
    }

    private BigDecimal calculateTorque(BigDecimal powerKw, BigDecimal rpm) {
        BigDecimal safeRpm = requirePositive(rpm, "shaft.rpm");
        return scale(TORQUE_CONSTANT.multiply(powerKw, MATH_CONTEXT).divide(safeRpm, MATH_CONTEXT));
    }

    private List<ShaftState> buildShaftStates(
            Motor selectedMotor,
            DesignConstantSet constantSet,
            BigDecimal beltRatioU1,
            BigDecimal bevelGearRatioU2,
            BigDecimal spurGearRatioU3
    ) {
        BigDecimal etaKn = requirePositive(constantSet.getEtaKn(), "constantSet.etaKn");
        BigDecimal etaD = requirePositive(constantSet.getEtaD(), "constantSet.etaD");
        BigDecimal etaBrc = requirePositive(constantSet.getEtaBrc(), "constantSet.etaBrc");
        BigDecimal etaBrt = requirePositive(constantSet.getEtaBrt(), "constantSet.etaBrt");
        BigDecimal etaOl = requirePositive(constantSet.getEtaOl(), "constantSet.etaOl");

        BigDecimal motorPower = scale(selectedMotor.getRatedPowerKw());
        BigDecimal motorRpm = scale(selectedMotor.getRatedRpm());

        // Temporary shaft-loss split for Module 1: etaOl is applied across three transitions so
        // the drum-side power remains consistent with the etaOl^3 total-efficiency formula.
        BigDecimal shaft1Power = scale(
                motorPower.multiply(etaKn, MATH_CONTEXT)
                        .multiply(etaD, MATH_CONTEXT)
                        .multiply(etaOl, MATH_CONTEXT)
        );
        BigDecimal shaft1Rpm = scale(divide(motorRpm, beltRatioU1));

        BigDecimal shaft2Power = scale(
                shaft1Power.multiply(etaBrc, MATH_CONTEXT)
                        .multiply(etaOl, MATH_CONTEXT)
        );
        BigDecimal shaft2Rpm = scale(divide(shaft1Rpm, bevelGearRatioU2));

        BigDecimal shaft3Power = scale(
                shaft2Power.multiply(etaBrt, MATH_CONTEXT)
                        .multiply(etaOl, MATH_CONTEXT)
        );
        BigDecimal shaft3Rpm = scale(divide(shaft2Rpm, spurGearRatioU3));

        List<ShaftState> shaftStates = new ArrayList<>();
        shaftStates.add(buildShaftState(ShaftCode.MOTOR, 1, motorPower, motorRpm));
        shaftStates.add(buildShaftState(ShaftCode.SHAFT_1, 2, shaft1Power, shaft1Rpm));
        shaftStates.add(buildShaftState(ShaftCode.SHAFT_2, 3, shaft2Power, shaft2Rpm));
        shaftStates.add(buildShaftState(ShaftCode.SHAFT_3, 4, shaft3Power, shaft3Rpm));
        shaftStates.add(buildShaftState(ShaftCode.DRUM_SHAFT, 5, shaft3Power, shaft3Rpm));
        return shaftStates;
    }

    private ShaftState buildShaftState(ShaftCode shaftCode, int sequenceNo, BigDecimal powerKw, BigDecimal rpm) {
        return ShaftState.builder()
                .shaftCode(shaftCode)
                .sequenceNo(sequenceNo)
                .powerKw(scale(powerKw))
                .rpm(scale(rpm))
                .torqueNmm(calculateTorque(powerKw, rpm))
                .build();
    }

    private void replaceExistingModule1Result(DesignCase designCase) {
        module1ResultRepository.findByDesignCaseId(designCase.getId())
                .ifPresent(existingResult -> {
                    designCase.setModule1Result(null);
                    module1ResultRepository.delete(existingResult);
                    module1ResultRepository.flush();
                });
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

    private Module1CalculationHistoryItemResponse mapToHistoryItem(Module1Result module1Result) {
        DesignCase designCase = module1Result.getDesignCase();
        Motor selectedMotor = module1Result.getSelectedMotor();

        return new Module1CalculationHistoryItemResponse(
                designCase.getId(),
                module1Result.getId(),
                MODULE_LABEL,
                designCase.getCaseCode(),
                designCase.getCaseName(),
                designCase.getRequiredPowerKw(),
                designCase.getRequiredOutputRpm(),
                selectedMotor.getMotorCode(),
                resolveMotorDisplayName(selectedMotor),
                module1Result.getCreatedAt(),
                module1Result.getUpdatedAt()
        );
    }

    private Module1CalculationResponse mapToResponse(Module1Result module1Result, long availableMotorsCount) {
        DesignCase designCase = module1Result.getDesignCase();
        DesignConstantSet constantSet = designCase.getConstantSet();
        Motor selectedMotor = module1Result.getSelectedMotor();

        Module1CalculationResponse.ResultInfo resultInfo = new Module1CalculationResponse.ResultInfo(
                module1Result.getId(),
                module1Result.getCreatedAt(),
                module1Result.getUpdatedAt()
        );

        Module1CalculationResponse.CaseInfo caseInfo = new Module1CalculationResponse.CaseInfo(
                designCase.getId(),
                designCase.getCaseCode(),
                designCase.getCaseName(),
                designCase.getStatus().name()
        );

        Module1CalculationResponse.InputSummary inputSummary = new Module1CalculationResponse.InputSummary(
                designCase.getRequiredPowerKw(),
                designCase.getRequiredOutputRpm()
        );

        Module1CalculationResponse.ReferenceSummary referenceSummary = new Module1CalculationResponse.ReferenceSummary(
                constantSet.getId(),
                constantSet.getSetCode(),
                constantSet.getSetName(),
                availableMotorsCount,
                constantSet.getDefaultBeltRatioU1(),
                constantSet.getDefaultGearboxRatioUh()
        );

        Module1CalculationResponse.SelectedMotorSummary selectedMotorSummary =
                new Module1CalculationResponse.SelectedMotorSummary(
                        selectedMotor.getId(),
                        selectedMotor.getMotorCode(),
                        resolveMotorDisplayName(selectedMotor),
                        normalizeText(selectedMotor.getManufacturer()),
                        normalizeText(selectedMotor.getDescription()),
                        selectedMotor.getRatedPowerKw(),
                        selectedMotor.getRatedRpm()
                );

        Module1CalculationResponse.TransmissionRatiosSummary transmissionRatios =
                new Module1CalculationResponse.TransmissionRatiosSummary(
                        module1Result.getTotalTransmissionRatioU(),
                        module1Result.getBeltRatioU1(),
                        module1Result.getGearboxTransmissionRatioUh(),
                        module1Result.getBevelGearRatioU2(),
                        module1Result.getSpurGearRatioU3()
                );

        List<Module1CalculationResponse.ShaftStateSummary> shaftStates = module1Result.getShaftStates().stream()
                .sorted(Comparator.comparingInt(ShaftState::getSequenceNo))
                .map(shaftState -> new Module1CalculationResponse.ShaftStateSummary(
                        shaftState.getShaftCode(),
                        resolveShaftLabel(shaftState.getShaftCode()),
                        shaftState.getSequenceNo(),
                        shaftState.getPowerKw(),
                        shaftState.getRpm(),
                        shaftState.getTorqueNmm()
                ))
                .toList();

        return new Module1CalculationResponse(
                resultInfo,
                caseInfo,
                inputSummary,
                referenceSummary,
                selectedMotorSummary,
                module1Result.getTotalEfficiency(),
                module1Result.getRequiredMotorPowerKw(),
                module1Result.getPreliminaryMotorRpmNsb(),
                transmissionRatios,
                shaftStates,
                deserializeNotes(module1Result.getCalculationNote())
        );
    }

    private List<String> buildCalculationNotes(DesignConstantSet constantSet) {
        return List.of(
                "Constant set " + constantSet.getSetCode() + " was used for this Module 1 calculation.",
                "Motor selection only considers active motors with rated power greater than or equal to the required motor power, then picks the rpm closest to the preliminary motor rpm.",
                "Shaft power propagation starts from the selected motor rated power and applies etaOl across three transitions so the shaft states remain consistent with etaKn * etaD * etaBrc * etaBrt * etaOl^3.",
                "Bevel gear ratio U2 = 3.14 is a temporary placeholder until Module 3 provides the real gearbox split.",
                "Recalculating an existing case replaces the previous Module 1 result and invalidates any downstream Module 3 result tied to the same design case."
        );
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

    private String resolveMotorDisplayName(Motor motor) {
        String description = normalizeText(motor.getDescription());
        return description != null ? description : motor.getMotorCode();
    }

    private String resolveShaftLabel(ShaftCode shaftCode) {
        return switch (shaftCode) {
            case MOTOR -> "Motor Shaft";
            case SHAFT_1 -> "Shaft 1";
            case SHAFT_2 -> "Shaft 2";
            case SHAFT_3 -> "Shaft 3";
            case DRUM_SHAFT -> "Output Drum Shaft";
        };
    }

    private BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.signum() <= 0) {
            throw new InvalidModule1InputException(fieldName + " must be greater than zero");
        }
        return value;
    }

    private BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(requirePositive(divisor, "divisor"), MATH_CONTEXT);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
