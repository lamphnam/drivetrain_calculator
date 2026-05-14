/**
 * Mock-only constants for Module 1 reference values.
 */

import type { Module1ReferenceValuesResponseDto } from '@/types/api/module1';

export const mockModule1ReferenceValues: Module1ReferenceValuesResponseDto = {
  constantSetId: 1,
  constantSetCode: 'DEFAULT_SET_V1',
  constantSetName: 'Default Constants V1',
  availableMotorsCount: 9,
  defaultBeltRatioU1: 2.5,
  defaultGearboxRatioUh: 8.0,
  defaultOverallEfficiency: 0.88,
};
