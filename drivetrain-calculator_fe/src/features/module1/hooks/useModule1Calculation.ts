/**
 * Lightweight integration hook for Module 1 API calls and UI-friendly error mapping.
 */

import { useCallback, useState } from 'react';

import { UI_TEXT } from '@/constants/uiText';
import { module1Api } from '@/services/api';
import { extractApiErrorResponse } from '@/utils/api';
import type { ApiErrorResponseDto } from '@/types/api/common';
import type {
  Module1CalculationRequestDto,
  Module1CalculationResponseDto,
  Module1ReferenceValuesResponseDto,
} from '@/types/api/module1';

type BootstrapData = {
  referenceValues: Module1ReferenceValuesResponseDto | null;
};

type UseModule1CalculationResult = {
  bootstrapData: BootstrapData;
  bootstrapError: string;
  isBootstrapping: boolean;
  isSubmitting: boolean;
  submissionError: string;
  submissionApiError: ApiErrorResponseDto | null;
  loadBootstrapData: () => Promise<void>;
  submitCalculation: (
    request: Module1CalculationRequestDto,
  ) => Promise<{
    result: Module1CalculationResponseDto | null;
    apiError: ApiErrorResponseDto | null;
    message: string;
  }>;
};

export function useModule1Calculation(): UseModule1CalculationResult {
  const [bootstrapData, setBootstrapData] = useState<BootstrapData>({
    referenceValues: null,
  });
  const [bootstrapError, setBootstrapError] = useState('');
  const [submissionError, setSubmissionError] = useState('');
  const [submissionApiError, setSubmissionApiError] = useState<ApiErrorResponseDto | null>(null);
  const [isBootstrapping, setIsBootstrapping] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadBootstrapData = useCallback(async () => {
    try {
      setIsBootstrapping(true);
      setBootstrapError('');

      const referenceValues = await module1Api.getReferenceValues();

      setBootstrapData({
        referenceValues,
      });
    } catch {
      setBootstrapError(UI_TEXT.states.bootstrapError);
    } finally {
      setIsBootstrapping(false);
    }
  }, []);

  const submitCalculation = useCallback(async (request: Module1CalculationRequestDto) => {
    try {
      setIsSubmitting(true);
      setSubmissionError('');
      setSubmissionApiError(null);

      const result = await module1Api.calculate(request);
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
    bootstrapData,
    bootstrapError,
    isBootstrapping,
    isSubmitting,
    submissionError,
    submissionApiError,
    loadBootstrapData,
    submitCalculation,
  };
}


function getUserFacingSubmissionMessage(apiError: ApiErrorResponseDto | null): string {
  switch (apiError?.error.code) {
    case 'VALIDATION_ERROR':
      return UI_TEXT.states.invalidInputMessage;
    case 'NO_SUITABLE_MOTOR':
      return UI_TEXT.states.noSuitableMotor;
    default:
      return UI_TEXT.states.unableToCalculate;
  }
}
