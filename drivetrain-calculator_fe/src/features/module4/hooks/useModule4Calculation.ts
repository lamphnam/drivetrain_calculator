/**
 * Lightweight integration hook for Module 4 API calls.
 */

import { useCallback, useState } from 'react';

import { UI_TEXT } from '@/constants/uiText';
import { module4Api } from '@/services/api';
import { extractApiErrorResponse } from '@/utils/api';
import type { ApiErrorResponseDto } from '@/types/api/common';
import type {
  Module4CalculationRequestDto,
  Module4CalculationResponseDto,
} from '@/types/api/module4';

type UseModule4CalculationResult = {
  isSubmitting: boolean;
  submissionError: string;
  submissionApiError: ApiErrorResponseDto | null;
  submitCalculation: (
    request: Module4CalculationRequestDto,
  ) => Promise<{
    result: Module4CalculationResponseDto | null;
    apiError: ApiErrorResponseDto | null;
    message: string;
  }>;
};

export function useModule4Calculation(): UseModule4CalculationResult {
  const [submissionError, setSubmissionError] = useState('');
  const [submissionApiError, setSubmissionApiError] = useState<ApiErrorResponseDto | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submitCalculation = useCallback(async (request: Module4CalculationRequestDto) => {
    try {
      setIsSubmitting(true);
      setSubmissionError('');
      setSubmissionApiError(null);

      const result = await module4Api.calculate(request);
      return {
        result,
        apiError: null,
        message: '',
      };
    } catch (error) {
      const apiError = extractApiErrorResponse(error);
      const message = getUserFacingSubmissionMessage(apiError);
      setSubmissionApiError(apiError);
      setSubmissionError(message);

      return {
        result: null,
        apiError,
        message,
      };
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  return {
    isSubmitting,
    submissionError,
    submissionApiError,
    submitCalculation,
  };
}


function getUserFacingSubmissionMessage(apiError: ApiErrorResponseDto | null): string {
  switch (apiError?.error.code) {
    case 'VALIDATION_ERROR':
      return UI_TEXT.states.invalidInputMessage;
    default:
      return UI_TEXT.states.unableToCalculate;
  }
}
