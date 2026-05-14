/**
 * Mock-only handler implementations for Module 4 endpoint behavior.
 */

import { wait } from '@/services/api/mocks/mockDelay';
import type {
  Module4CalculationRequestDto,
  Module4CalculationResponseDto,
} from '@/types/api/module4';

const MOCK_RESPONSE_DELAY_MS = 300;

export async function calculateModule4Mock(
  request: Module4CalculationRequestDto,
): Promise<Module4CalculationResponseDto> {
  await wait(MOCK_RESPONSE_DELAY_MS * 2);

  // Plausible values matching the API contract example
  return {
    resultInfo: {
      resultId: Math.floor(Math.random() * 1000) + 4000,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    },
    caseInfo: {
      designCaseId: request.designCaseId,
      caseCode: `FULL-TEST-${request.designCaseId}`,
      caseName: 'Module 4 Calculation',
      status: 'MODULE4_COMPLETED',
    },
    inputSummary: {
      inputT2Nmm: request.inputT2Nmm ?? 185000.0,
      inputN2Rpm: request.inputN2Rpm ?? 980.0,
      inputU3: request.inputU3 ?? 3.2,
      allowableContactStressMpa: request.allowableContactStressMpa ?? 650.0,
      allowableBendingStressGear1Mpa: request.allowableBendingStressGear1Mpa ?? 280.0,
      allowableBendingStressGear2Mpa: request.allowableBendingStressGear2Mpa ?? 260.0,
    },
    spurGearGeometry: {
      centerDistanceAwMm: 121.43,
      moduleMSelected: 2.5,
      teethZ1: 19,
      teethZ2: 61,
      actualRatioU3: 3.210526,
      ratioErrorPercent: 0.329563,
      diameterDw1Mm: 57.8,
      diameterDw2Mm: 185.1,
      widthBwMm: 30.36,
    },
    derivedFactors: {
      epsilonAlpha: 1.402,
      zEpsilon: 0.944,
      yEpsilon: 0.713,
      yF1: 3.8,
      yF2: 3.6,
      loadFactorKh: 1.061,
      loadFactorKf: 1.177,
    },
    stressCheck: {
      sigmaHMpa: 512.4,
      sigmaF1Mpa: 189.7,
      sigmaF2Mpa: 179.7,
      contactStressPass: true,
      bendingStressGear1Pass: true,
      bendingStressGear2Pass: true,
    },
    shaftForces: [
      {
        shaftCode: 'SHAFT_2',
        shaftLabel: 'Shaft 2',
        ftN: 6400.0,
        frN: 2331.0,
        faN: 0.0,
      },
      {
        shaftCode: 'SHAFT_3',
        shaftLabel: 'Shaft 3',
        ftN: 6400.0,
        frN: 2331.0,
        faN: 0.0,
      },
    ],
    calculationNotes: [
      'Input torque T2 was inherited from Module 1 shaft state SHAFT_2.',
      'Input speed n2 was inherited from Module 1 shaft state SHAFT_2.',
      'Input spur ratio U3 was inherited from stored Module 1 ratio U3.',
      'Axial force Fa = 0 because straight spur gear teeth produce no axial thrust.',
    ],
  };
}

export async function getModule4HistoryMock(designCaseId: number): Promise<Module4CalculationResponseDto> {
  return calculateModule4Mock({ designCaseId });
}
