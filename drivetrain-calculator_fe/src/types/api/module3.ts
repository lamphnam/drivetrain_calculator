import type { ShaftForceDto } from './common';

export interface Module3MaterialDto {
  materialId: number;
  materialCode: string;
  materialName: string;
  heatTreatment: string | null;
  hbMin: number;
  hbMax: number;
  sigmaBMpa: number;
  sigmaChMpa: number;
}

export type Module3MaterialsResponseDto = Module3MaterialDto[];

export interface Module3CalculationRequestDto {
  designCaseId: number;
  inputT1Nmm?: number;
  inputN1Rpm?: number;
  inputU2?: number;
  serviceLifeHours?: number;
  materialId: number;
}

export interface Module3CalculationResponseDto {
  resultInfo: {
    resultId: number;
    createdAt: string;
    updatedAt: string;
  };
  caseInfo: {
    designCaseId: number;
    caseCode: string;
    caseName: string;
    status: string;
  };
  inputSummary: {
    inputT1Nmm: number;
    inputN1Rpm: number;
    inputU2: number;
    serviceLifeHours: number;
  };
  selectedMaterial: Module3MaterialDto;
  allowableStresses: {
    allowableContactStressMpa: number;
    allowableBendingStressMpa: number;
  };
  gearGeometry: {
    reCalculated: number;
    de1Calculated: number;
    moduleMteSelected: number;
    teethZ1: number;
    teethZ2: number;
    actualRatioU2: number;
    widthBMm: number;
    diameterDm1Mm: number;
    diameterDm2Mm: number;
    coneAngleDelta1Deg: number;
    coneAngleDelta2Deg: number;
  };
  stressCheck: {
    sigmaHMpa: number;
    sigmaF1Mpa: number;
    sigmaF2Mpa: number;
    contactStressPass: boolean;
    bendingStressPass: boolean;
  };
  shaftForces: ShaftForceDto[];
  calculationNotes: string[];
}
