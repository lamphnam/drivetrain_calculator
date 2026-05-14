/**
 * Shared API DTOs and error contracts that both FE and BE should treat as stable.
 */

export interface ApiFieldErrorDto {
  field: string;
  reason: string;
}

export type ApiErrorCode =
  | 'VALIDATION_ERROR'
  | 'NO_SUITABLE_MOTOR'
  | 'NOT_FOUND'
  | 'INTERNAL_ERROR';

export interface ApiErrorDto {
  code: ApiErrorCode;
  message: string;
  details?: Record<string, unknown>;
  fieldErrors?: ApiFieldErrorDto[];
}

export interface ApiErrorResponseDto {
  error: ApiErrorDto;
}

export interface ShaftForceDto {
  shaftCode: string;
  shaftLabel: string;
  ftN: number;
  frN: number;
  faN: number;
}

export interface ApiRequestOptions {
  headers?: Record<string, string>;
  signal?: AbortSignal;
}
