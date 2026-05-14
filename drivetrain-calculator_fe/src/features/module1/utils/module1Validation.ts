/**
 * Form validation helpers for the Module 1 input flow.
 */

import { UI_TEXT } from '@/constants/uiText';
import type { ApiFieldErrorDto } from '@/types/api/common';
import type { Module1CalculationRequestDto } from '@/types/api/module1';

export type Module1FormValues = {
  powerKw: string;
  outputRpm: string;
};

export type Module1FormErrors = {
  powerKw?: string;
  outputRpm?: string;
};

export function validateModule1Form(values: Module1FormValues): Module1FormErrors {
  const errors: Module1FormErrors = {};
  const powerKw = Number(values.powerKw);
  const outputRpm = Number(values.outputRpm);

  if (!values.powerKw.trim()) {
    errors.powerKw = UI_TEXT.states.invalidInputDescription;
  } else if (Number.isNaN(powerKw)) {
    errors.powerKw = UI_TEXT.states.invalidInputDescription;
  } else if (powerKw <= 0) {
    errors.powerKw = UI_TEXT.states.invalidInputDescription;
  }

  if (!values.outputRpm.trim()) {
    errors.outputRpm = UI_TEXT.states.invalidInputDescription;
  } else if (Number.isNaN(outputRpm)) {
    errors.outputRpm = UI_TEXT.states.invalidInputDescription;
  } else if (outputRpm <= 0) {
    errors.outputRpm = UI_TEXT.states.invalidInputDescription;
  }

  return errors;
}

export function buildModule1CalculationRequest(
  values: Module1FormValues,
): Module1CalculationRequestDto {
  return {
    requiredPowerKw: Number(values.powerKw),
    requiredOutputRpm: Number(values.outputRpm),
  };
}

export function mapApiFieldErrorsToFormErrors(
  fieldErrors?: ApiFieldErrorDto[],
): Module1FormErrors {
  const errors: Module1FormErrors = {};

  for (const fieldError of fieldErrors ?? []) {
    if (fieldError.field === 'requiredPowerKw') {
      errors.powerKw = fieldError.reason;
    }

    if (fieldError.field === 'requiredOutputRpm') {
      errors.outputRpm = fieldError.reason;
    }
  }

  return errors;
}
