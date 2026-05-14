/**
 * Public Module 3 API contract that frontend services and future HTTP implementations should follow.
 */

import type {
  Module3CalculationRequestDto,
  Module3CalculationResponseDto,
  Module3MaterialsResponseDto,
} from '@/types/api/module3';

export const module3ApiEndpoints = {
  getMaterials: '/api/v1/module-3/materials',
  calculate: '/api/v1/module-3/calculate',
  getHistory: (designCaseId: number) => `/api/v1/module-3/history/${designCaseId}`,
} as const;

export interface Module3ApiContract {
  getMaterials(): Promise<Module3MaterialsResponseDto>;
  calculate(request: Module3CalculationRequestDto): Promise<Module3CalculationResponseDto>;
  getHistory(designCaseId: number): Promise<Module3CalculationResponseDto>;
}
