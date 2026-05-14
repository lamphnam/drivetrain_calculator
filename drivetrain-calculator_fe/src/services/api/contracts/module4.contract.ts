/**
 * Public Module 4 API contract that frontend services and future HTTP implementations should follow.
 */

import type {
  Module4CalculationRequestDto,
  Module4CalculationResponseDto,
} from '@/types/api/module4';

export const module4ApiEndpoints = {
  calculate: '/api/v1/module-4/calculate',
  getHistory: (designCaseId: number) => `/api/v1/module-4/history/${designCaseId}`,
} as const;

export interface Module4ApiContract {
  calculate(request: Module4CalculationRequestDto): Promise<Module4CalculationResponseDto>;
  getHistory(designCaseId: number): Promise<Module4CalculationResponseDto>;
}
