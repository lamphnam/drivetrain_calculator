/**
 * Mock-only error helpers that enforce one stable API error shape for FE and BE alignment.
 */

import type { ApiErrorCode, ApiErrorResponseDto, ApiFieldErrorDto } from '@/types/api/common';

export class MockApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly response: ApiErrorResponseDto,
  ) {
    super(response.error.message);
    this.name = 'MockApiError';
  }
}

export function createApiErrorResponse(
  code: ApiErrorCode,
  message: string,
  options?: {
    details?: Record<string, unknown>;
    fieldErrors?: ApiFieldErrorDto[];
  },
): ApiErrorResponseDto {
  return {
    error: {
      code,
      message,
      details: options?.details,
      fieldErrors: options?.fieldErrors,
    },
  };
}

export function throwValidationError(fieldErrors: ApiFieldErrorDto[]): never {
  throw new MockApiError(
    400,
    createApiErrorResponse('VALIDATION_ERROR', 'Request validation failed.', {
      fieldErrors,
    }),
  );
}

export function throwNoSuitableMotorError(requiredMotorPowerKw: number): never {
  throw new MockApiError(
    422,
    createApiErrorResponse(
      'NO_SUITABLE_MOTOR',
      'No suitable motor was found for the required power and speed range.',
      {
        details: {
          requiredMotorPowerKw,
        },
      },
    ),
  );
}
