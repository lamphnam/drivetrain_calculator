/**
 * Lightweight integration hook for Module 3 API calls.
 */

import { useCallback, useState } from 'react';

import { UI_TEXT } from '@/constants/uiText';
import { module3Api } from '@/services/api';
import { extractApiErrorResponse } from '@/utils/api';
import type { ApiErrorResponseDto } from '@/types/api/common';
import type {
  Module3CalculationRequestDto,
  Module3CalculationResponseDto,
  Module3MaterialDto,
} from '@/types/api/module3';

type BootstrapData = {
  materials: Module3MaterialDto[];
};

type UseModule3CalculationResult = {
  bootstrapData: BootstrapData;
  bootstrapError: string;
  isBootstrapping: boolean;
  isSubmitting: boolean;
  submissionError: string;
  submissionApiError: ApiErrorResponseDto | null;
  loadBootstrapData: () => Promise<void>;
  submitCalculation: (
    request: Module3CalculationRequestDto,
  ) => Promise<{
    result: Module3CalculationResponseDto | null;
    apiError: ApiErrorResponseDto | null;
    message: string;
  }>;
};

export function useModule3Calculation(): UseModule3CalculationResult {
  const [bootstrapData, setBootstrapData] = useState<BootstrapData>({
    materials: [],
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

      const materials = await module3Api.getMaterials();

      setBootstrapData({
        materials,
      });
    } catch {
      setBootstrapError(UI_TEXT.states.bootstrapError);
    } finally {
      setIsBootstrapping(false);
    }
  }, []);

  const submitCalculation = useCallback(async (request: Module3CalculationRequestDto) => {
    try {
      setIsSubmitting(true);
      setSubmissionError('');
      setSubmissionApiError(null);

      const result = await module3Api.calculate(request);
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
    default:
      return UI_TEXT.states.unableToCalculate;
  }
}
