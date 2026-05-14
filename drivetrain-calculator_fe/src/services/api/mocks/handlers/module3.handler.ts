/**
 * Mock-only handler implementations for Module 3 endpoint behavior.
 */

import { wait } from '@/services/api/mocks/mockDelay';
import type {
  Module3CalculationRequestDto,
  Module3CalculationResponseDto,
  Module3MaterialDto,
  Module3MaterialsResponseDto,
} from '@/types/api/module3';

const MOCK_RESPONSE_DELAY_MS = 300;

export const mockModule3Materials: Module3MaterialDto[] = [
  {
    materialId: 1,
    materialCode: 'C40XH_QT',
    materialName: 'Steel C40XH',
    heatTreatment: 'Quenched and tempered',
    hbMin: 235,
    hbMax: 262,
    sigmaBMpa: 850,
    sigmaChMpa: 650,
  },
  {
    materialId: 2,
    materialCode: '40CR_N',
    materialName: 'Steel 40Cr',
    heatTreatment: 'Normalized',
    hbMin: 207,
    hbMax: 241,
    sigmaBMpa: 780,
    sigmaChMpa: 540,
  },
  {
    materialId: 3,
    materialCode: 'C45_N',
    materialName: 'Steel C45',
    heatTreatment: 'Normalized',
    hbMin: 179,
    hbMax: 207,
    sigmaBMpa: 600,
    sigmaChMpa: 355,
  },
];

export async function getModule3MaterialsMock(): Promise<Module3MaterialsResponseDto> {
  await wait(MOCK_RESPONSE_DELAY_MS);
  return mockModule3Materials;
}

export async function calculateModule3Mock(
  request: Module3CalculationRequestDto,
): Promise<Module3CalculationResponseDto> {
  await wait(MOCK_RESPONSE_DELAY_MS * 2);

  const material = mockModule3Materials.find((m) => m.materialId === request.materialId) || mockModule3Materials[0];

  // Plausible values matching the API contract example
  return {
    resultInfo: {
      resultId: Math.floor(Math.random() * 1000),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    },
    caseInfo: {
      designCaseId: request.designCaseId,
      caseCode: `CASE-M3-${request.designCaseId}`,
      caseName: 'Module 3 Calculation',
      status: 'MODULE3_COMPLETED',
    },
    inputSummary: {
      inputT1Nmm: request.inputT1Nmm ?? 83851.991,
      inputN1Rpm: request.inputN1Rpm ?? 812,
      inputU2: request.inputU2 ?? 3.14,
      serviceLifeHours: request.serviceLifeHours ?? 43200,
    },
    selectedMaterial: material,
    allowableStresses: {
      allowableContactStressMpa: 608.94,
      allowableBendingStressMpa: 258.4,
    },
    gearGeometry: {
      reCalculated: 133.912986,
      de1Calculated: 81.0,
      moduleMteSelected: 3.0,
      teethZ1: 27,
      teethZ2: 85,
      actualRatioU2: 3.148148,
      widthBMm: 38.0,
      diameterDm1Mm: 69.495849,
      diameterDm2Mm: 218.783229,
      coneAngleDelta1Deg: 17.622297,
      coneAngleDelta2Deg: 72.377703,
    },
    stressCheck: {
      sigmaHMpa: 556.244921,
      sigmaF1Mpa: 92.382408,
      sigmaF2Mpa: 88.730929,
      contactStressPass: true,
      bendingStressPass: true,
    },
    shaftForces: [
      {
        shaftCode: 'SHAFT_1',
        shaftLabel: 'Shaft 1',
        ftN: 2413.151065,
        frN: 837.098398,
        faN: 265.901841,
      },
      {
        shaftCode: 'SHAFT_2',
        shaftLabel: 'Shaft 2',
        ftN: 2413.151065,
        frN: 265.901841,
        faN: 837.098398,
      },
    ],
    calculationNotes: [
      'Input values were resolved from Module 1 defaults or overrides.',
      'Allowable stress evaluation uses simplified life-factor approximations.',
      'Selected module and tooth counts keep the actual ratio within 4% of target.',
    ],
  };
}

export async function getModule3HistoryMock(designCaseId: number): Promise<Module3CalculationResponseDto> {
  return calculateModule3Mock({ designCaseId, materialId: 1 });
}
