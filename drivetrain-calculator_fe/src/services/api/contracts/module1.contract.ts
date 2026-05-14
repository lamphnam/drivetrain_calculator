/**
 * Public Module 1 API contract matching the Spring Boot backend endpoints.
 */

import type { ApiErrorResponseDto } from '@/types/api/common';
import type {
  Module1CalculationRequestDto,
  Module1CalculationResponseDto,
  Module1CalculationHistoryItemDto,
  Module1ReferenceValuesResponseDto,
} from '@/types/api/module1';

export const module1ApiEndpoints = {
  calculate: '/api/v1/module-1/calculate',
  referenceValues: '/api/v1/module-1/reference-values',
  history: '/api/v1/module-1/history',
  historyById: (designCaseId: number) => `/api/v1/module-1/history/${designCaseId}`,
} as const;

export interface Module1ApiContract {
  getReferenceValues(constantSetId?: number): Promise<Module1ReferenceValuesResponseDto>;
  calculate(request: Module1CalculationRequestDto): Promise<Module1CalculationResponseDto>;
  getHistory(): Promise<Module1CalculationHistoryItemDto[]>;
  getHistoryById(designCaseId: number): Promise<Module1CalculationResponseDto>;
}

export interface Module1ApiExamples {
  calculateSuccess: Module1CalculationResponseDto;
  calculateValidationError: ApiErrorResponseDto;
  calculateNoMotorError: ApiErrorResponseDto;
}
