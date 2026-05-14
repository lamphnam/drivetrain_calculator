/**
 * Shared calculation-related domain types that may be reused across features.
 */

export type CalculationModule = 'module1';
export type CalculationStatus = 'draft' | 'completed';

export interface SavedCalculationSummary {
  id: string;
  module: CalculationModule;
  name: string;
  updatedAt: string;
  status: CalculationStatus;
}
