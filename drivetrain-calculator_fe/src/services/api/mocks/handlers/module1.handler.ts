/**
 * Mock-only handler implementations for Module 1 endpoint behavior.
 */

import { wait } from '@/services/api/mocks/mockDelay';
import { throwNoSuitableMotorError, throwValidationError } from '@/services/api/mocks/mockErrors';
import type {
  Module1CalculationRequestDto,
  Module1CalculationResponseDto,
  Module1ReferenceValuesResponseDto,
  ShaftStateSummaryDto,
  TransmissionRatiosSummaryDto,
} from '@/types/api/module1';

const MOCK_RESPONSE_DELAY_MS = 280;

const MOCK_MOTORS = [
  { motorId: 1, motorCode: 'K132S4', displayName: 'K132S4 - 5.5kW', manufacturer: 'Siemens', description: '3-phase async motor', ratedPowerKw: 5.5, ratedRpm: 1450 },
  { motorId: 2, motorCode: 'K132M4', displayName: 'K132M4 - 7.5kW', manufacturer: 'Siemens', description: '3-phase async motor', ratedPowerKw: 7.5, ratedRpm: 1450 },
  { motorId: 3, motorCode: 'K160S4', displayName: 'K160S4 - 11kW', manufacturer: 'Siemens', description: '3-phase async motor', ratedPowerKw: 11, ratedRpm: 1460 },
  { motorId: 4, motorCode: 'K160M4', displayName: 'K160M4 - 15kW', manufacturer: 'Siemens', description: '3-phase async motor', ratedPowerKw: 15, ratedRpm: 1460 },
  { motorId: 5, motorCode: 'K180S4', displayName: 'K180S4 - 18.5kW', manufacturer: 'Siemens', description: '3-phase async motor', ratedPowerKw: 18.5, ratedRpm: 1470 },
];

export async function getReferenceValuesMock(): Promise<Module1ReferenceValuesResponseDto> {
  await wait(MOCK_RESPONSE_DELAY_MS);
  return {
    constantSetId: 1,
    constantSetCode: 'DEFAULT_SET_V1',
    constantSetName: 'Default Constants V1',
    availableMotorsCount: MOCK_MOTORS.length,
    defaultBeltRatioU1: 2.5,
    defaultGearboxRatioUh: 8.0,
    defaultOverallEfficiency: 0.88,
  };
}

export async function createModule1CalculationMock(
  request: Module1CalculationRequestDto,
): Promise<Module1CalculationResponseDto> {
  await wait(MOCK_RESPONSE_DELAY_MS);

  validateRequest(request);

  const systemEfficiency = 0.88;
  const requiredMotorPowerKw = roundDecimal(request.requiredPowerKw / systemEfficiency);
  const selectedMotor = selectMotor(requiredMotorPowerKw);
  const overallRatio = roundDecimal(selectedMotor.ratedRpm / request.requiredOutputRpm);
  const beltRatioU1 = 2.5;
  const bevelRatioU2 = 3.14;
  const spurRatioU3 = roundDecimal(overallRatio / (beltRatioU1 * bevelRatioU2));

  const transmissionRatios: TransmissionRatiosSummaryDto = {
    overallRatio,
    beltRatioU1,
    gearboxRatioUh: roundDecimal(bevelRatioU2 * spurRatioU3),
    bevelRatioU2,
    spurRatioU3,
  };

  const shaftStates = buildShaftStates(request.requiredPowerKw, selectedMotor.ratedRpm, transmissionRatios);
  const designCaseId = Math.floor(Math.random() * 10000) + 1;

  return {
    resultInfo: {
      resultId: designCaseId,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    },
    caseInfo: {
      designCaseId,
      caseCode: `CASE-${designCaseId}`,
      caseName: request.caseName || 'Module 1 Calculation',
      status: 'MODULE1_COMPLETED',
    },
    inputSummary: {
      requiredPowerKw: request.requiredPowerKw,
      requiredOutputRpm: request.requiredOutputRpm,
    },
    referenceSummary: {
      constantSetId: 1,
      constantSetCode: 'DEFAULT_SET_V1',
      constantSetName: 'Default Constants V1',
      availableMotorsCount: MOCK_MOTORS.length,
      defaultBeltRatioU1: beltRatioU1,
      defaultGearboxRatioUh: 8.0,
    },
    selectedMotor,
    systemEfficiency,
    requiredMotorPowerKw,
    preliminaryMotorRpmNsb: selectedMotor.ratedRpm,
    transmissionRatios,
    shaftStates,
    calculationNotes: [
      'Motor selected based on minimum rated power >= required motor power.',
      'Bevel gear ratio U2 = 3.14 is a placeholder value.',
    ],
  };
}

function validateRequest(request: Module1CalculationRequestDto): void {
  const fieldErrors = [];

  if (!(request.requiredPowerKw > 0)) {
    fieldErrors.push({ field: 'requiredPowerKw', reason: 'Must be greater than 0.' });
  }
  if (!(request.requiredOutputRpm > 0)) {
    fieldErrors.push({ field: 'requiredOutputRpm', reason: 'Must be greater than 0.' });
  }

  if (fieldErrors.length > 0) {
    throwValidationError(fieldErrors);
  }
}

function selectMotor(requiredPowerKw: number) {
  const motor = MOCK_MOTORS.find((m) => m.ratedPowerKw >= requiredPowerKw);
  if (!motor) {
    throwNoSuitableMotorError(requiredPowerKw);
  }
  return motor!;
}

function buildShaftStates(
  powerKw: number,
  motorRpm: number,
  ratios: TransmissionRatiosSummaryDto,
): ShaftStateSummaryDto[] {
  const shaft1Rpm = roundDecimal(motorRpm / ratios.beltRatioU1);
  const shaft2Rpm = roundDecimal(shaft1Rpm / ratios.bevelRatioU2);
  const shaft3Rpm = roundDecimal(shaft2Rpm / ratios.spurRatioU3);

  return [
    { shaftCode: 'MOTOR', shaftLabel: 'Motor Shaft', sequenceNo: 0, powerKw: roundDecimal(powerKw), rpm: motorRpm, torqueNmm: torque(powerKw, motorRpm) },
    { shaftCode: 'SHAFT_1', shaftLabel: 'Shaft 1', sequenceNo: 1, powerKw: roundDecimal(powerKw), rpm: shaft1Rpm, torqueNmm: torque(powerKw, shaft1Rpm) },
    { shaftCode: 'SHAFT_2', shaftLabel: 'Shaft 2', sequenceNo: 2, powerKw: roundDecimal(powerKw), rpm: shaft2Rpm, torqueNmm: torque(powerKw, shaft2Rpm) },
    { shaftCode: 'SHAFT_3', shaftLabel: 'Shaft 3', sequenceNo: 3, powerKw: roundDecimal(powerKw), rpm: shaft3Rpm, torqueNmm: torque(powerKw, shaft3Rpm) },
    { shaftCode: 'DRUM_SHAFT', shaftLabel: 'Drum Shaft', sequenceNo: 4, powerKw: roundDecimal(powerKw), rpm: shaft3Rpm, torqueNmm: torque(powerKw, shaft3Rpm) },
  ];
}

function torque(powerKw: number, rpm: number): number {
  return rpm > 0 ? roundDecimal((9_550_000 * powerKw) / rpm) : 0;
}

function roundDecimal(value: number, digits = 4): number {
  return Number(value.toFixed(digits));
}
