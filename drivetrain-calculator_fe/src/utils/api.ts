import type { ApiErrorResponseDto, ApiErrorCode } from '@/types/api/common';

/**
 * Normalizes various error shapes from the backend into the stable mobile error contract.
 */
export function extractApiErrorResponse(error: unknown): ApiErrorResponseDto | null {
  if (typeof error !== 'object' || error === null || !('response' in error)) {
    return null;
  }

  const response = (error as any).response;

  // Case 1: Mobile-specific shape { error: { code, message, ... } }
  if (response.error && typeof response.error === 'object' && response.error.code) {
    return response as ApiErrorResponseDto;
  }

  // Case 2: Standard Spring Boot error shape { status, error, message, path, ... }
  if (response.status && typeof response.error === 'string') {
    return {
      error: {
        code: mapHttpStatusToErrorCode(response.status),
        message: response.message || response.error,
        details: {
          path: response.path,
          status: response.status,
        },
      },
    };
  }

  return null;
}

function mapHttpStatusToErrorCode(status: number): ApiErrorCode {
  switch (status) {
    case 400:
    case 422:
      return 'VALIDATION_ERROR';
    case 404:
      return 'NOT_FOUND';
    default:
      return 'INTERNAL_ERROR';
  }
}
