/**
 * Module 1 API request and response DTOs matching the Spring Boot backend.
 */

export interface Module1CalculationRequestDto {
  requiredPowerKw: number;
  requiredOutputRpm: number;
  constantSetId?: number;
  caseCode?: string;
  caseName?: string;
}

export interface Module1ReferenceValuesResponseDto {
  constantSetId: number;
  constantSetCode: string;
  constantSetName: string;
  availableMotorsCount: number;
  defaultBeltRatioU1: number;
  defaultGearboxRatioUh: number;
  defaultOverallEfficiency: number;
}

export interface Module1CalculationHistoryItemDto {
  designCaseId: number;
  resultId: number;
  moduleLabel: string;
  caseCode: string;
  caseName: string;
  requiredPowerKw: number;
  requiredOutputRpm: number;
  selectedMotorCode: string;
  selectedMotorDisplayName: string;
  savedAt: string;
  updatedAt: string;
}

export interface ResultInfoDto {
  resultId: number;
  createdAt: string;
  updatedAt: string;
}

export interface CaseInfoDto {
  designCaseId: number;
  caseCode: string;
  caseName: string;
  status: string;
}

export interface InputSummaryDto {
  requiredPowerKw: number;
  requiredOutputRpm: number;
}

export interface ReferenceSummaryDto {
  constantSetId: number;
  constantSetCode: string;
  constantSetName: string;
  availableMotorsCount: number;
  defaultBeltRatioU1: number;
  defaultGearboxRatioUh: number;
}

export interface SelectedMotorSummaryDto {
  motorId: number;
  motorCode: string;
  displayName: string;
  manufacturer: string;
  description: string;
  ratedPowerKw: number;
  ratedRpm: number;
}

export interface TransmissionRatiosSummaryDto {
  overallRatio: number;
  beltRatioU1: number;
  gearboxRatioUh: number;
  bevelRatioU2: number;
  spurRatioU3: number;
}

export interface ShaftStateSummaryDto {
  shaftCode: string;
  shaftLabel: string;
  sequenceNo: number;
  powerKw: number;
  rpm: number;
  torqueNmm: number;
}

export interface Module1CalculationResponseDto {
  resultInfo: ResultInfoDto;
  caseInfo: CaseInfoDto;
  inputSummary: InputSummaryDto;
  referenceSummary: ReferenceSummaryDto;
  selectedMotor: SelectedMotorSummaryDto;
  systemEfficiency: number;
  requiredMotorPowerKw: number;
  preliminaryMotorRpmNsb: number;
  transmissionRatios: TransmissionRatiosSummaryDto;
  shaftStates: ShaftStateSummaryDto[];
  calculationNotes: string[];
}
