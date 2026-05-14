import type { ShaftForceDto } from './common';

export interface Module4CalculationRequestDto {
  designCaseId: number;
  inputT2Nmm?: number;
  inputN2Rpm?: number;
  inputU3?: number;
  allowableContactStressMpa?: number;
  allowableBendingStressGear1Mpa?: number;
  allowableBendingStressGear2Mpa?: number;
}

export interface Module4CalculationResponseDto {
  resultInfo: {
    resultId: number;
    createdAt: string;
    updatedAt: string;
  };
  caseInfo: {
    designCaseId: number;
    caseCode: string | null;
    caseName: string | null;
    status: string;
  };
  inputSummary: {
    inputT2Nmm: number;
    inputN2Rpm: number;
    inputU3: number;
    allowableContactStressMpa: number;
    allowableBendingStressGear1Mpa: number;
    allowableBendingStressGear2Mpa: number;
  };
  spurGearGeometry: {
    centerDistanceAwMm: number;
    moduleMSelected: number;
    teethZ1: number;
    teethZ2: number;
    actualRatioU3: number;
    ratioErrorPercent: number;
    diameterDw1Mm: number;
    diameterDw2Mm: number;
    widthBwMm: number;
  };
  derivedFactors: {
    epsilonAlpha: number;
    zEpsilon: number;
    yEpsilon: number;
    yF1: number;
    yF2: number;
    loadFactorKh: number;
    loadFactorKf: number;
  };
  stressCheck: {
    sigmaHMpa: number;
    sigmaF1Mpa: number;
    sigmaF2Mpa: number;
    contactStressPass: boolean;
    bendingStressGear1Pass: boolean;
    bendingStressGear2Pass: boolean;
  };
  shaftForces: ShaftForceDto[];
  calculationNotes: string[];
}
