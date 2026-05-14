/**
 * Domain types reserved for Module 1 input, output, and calculation lifecycle contracts.
 */

export interface Module1InputDraft {
  name: string;
  notes?: string;
}

export interface Module1ResultSnapshot {
  summary: string;
  transmissionRatio?: number;
}
