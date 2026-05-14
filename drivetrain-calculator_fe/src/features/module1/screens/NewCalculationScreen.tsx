import { useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';

import { ScreenContainer } from '@/components/layout/ScreenContainer';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { InlineError } from '@/components/ui/InlineError';
import { Input } from '@/components/ui/Input';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { LoadingState } from '@/components/ui/LoadingState';
import { Section } from '@/components/ui/Section';
import { StepIndicator } from '@/components/ui/StepIndicator';
import { Text } from '@/components/ui/Text';
import { routes } from '@/constants/routes';
import { UI_TEXT } from '@/constants/uiText';
import { useModule1Calculation } from '@/features/module1/hooks/useModule1Calculation';
import { addModule1HistoryEntry } from '@/features/module1/state/module1HistoryStore';
import { formatRatio } from '@/features/module1/utils/formatters';
import {
  buildModule1CalculationRequest,
  mapApiFieldErrorsToFormErrors,
  type Module1FormErrors,
  validateModule1Form,
} from '@/features/module1/utils/module1Validation';
import { appTheme } from '@/theme';

type SubmissionState = 'idle' | 'submitting' | 'error';

export function NewCalculationScreen() {
  const router = useRouter();
  const [powerKw, setPowerKw] = useState('');
  const [outputRpm, setOutputRpm] = useState('');
  const [formErrors, setFormErrors] = useState<Module1FormErrors>({});
  const [submissionState, setSubmissionState] = useState<SubmissionState>('idle');
  const {
    bootstrapData,
    bootstrapError,
    isBootstrapping,
    isSubmitting,
    submissionError,
    submissionApiError,
    loadBootstrapData,
    submitCalculation,
  } = useModule1Calculation();

  useEffect(() => {
    loadBootstrapData();
  }, [loadBootstrapData]);

  async function handleSubmit() {
    const nextErrors = validateModule1Form({ powerKw, outputRpm });
    setFormErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      setSubmissionState('error');
      return;
    }

    setSubmissionState('submitting');
    const submission = await submitCalculation(
      buildModule1CalculationRequest({ powerKw, outputRpm }),
    );

    if (submission.result) {
      const historyEntry = addModule1HistoryEntry(submission.result);
      router.push({
        pathname: routes.calculationsResult,
        params: {
          requestId: historyEntry.id,
        },
      });
      return;
    }

    if (submission.apiError?.error.fieldErrors) {
      setFormErrors((currentErrors) => ({
        ...currentErrors,
        ...mapApiFieldErrorsToFormErrors(submission.apiError?.error.fieldErrors),
      }));
    }

    if (submission.message || submissionError || submissionApiError) {
      setSubmissionState('error');
    }
  }

  function handleReset() {
    setPowerKw('');
    setOutputRpm('');
    setFormErrors({});
    setSubmissionState('idle');
  }

  return (
    <ScreenContainer>
      <Section
        eyebrow="Module 1"
        title="Motor Selection"
        description="Enter the required output power and speed to select a motor and calculate transmission ratios."
      />

      <StepIndicator
        steps={[
          { label: 'Motor', active: true },
          { label: 'Bevel Gear' },
          { label: 'Spur Gear' },
        ]}
      />

      {/* Reference values */}
      <Card
        title={UI_TEXT.newCalculation.referenceValuesTitle}
        description="System defaults loaded from the backend.">
        {isBootstrapping ? (
          <LoadingState
            title={UI_TEXT.newCalculation.bootstrapLoadingTitle}
            description={UI_TEXT.newCalculation.bootstrapLoadingDescription}
          />
        ) : bootstrapError ? (
          <InlineError message={bootstrapError} />
        ) : bootstrapData.referenceValues ? (
          <KeyValueList
            items={[
              {
                label: 'Available Motors',
                value: `${bootstrapData.referenceValues.availableMotorsCount}`,
                valueVariant: 'bodySmallStrong',
              },
              {
                label: 'Belt Ratio (U1)',
                value: formatRatio(bootstrapData.referenceValues.defaultBeltRatioU1),
              },
              {
                label: 'Gearbox Ratio (Uh)',
                value: formatRatio(bootstrapData.referenceValues.defaultGearboxRatioUh),
              },
              {
                label: 'Overall Efficiency',
                value: formatRatio(bootstrapData.referenceValues.defaultOverallEfficiency),
              },
            ]}
          />
        ) : null}
      </Card>

      {/* Input form */}
      <Card title="Required Inputs">
        <Input
          keyboardType="decimal-pad"
          label="Required Power"
          value={powerKw}
          onChangeText={setPowerKw}
          placeholder="e.g. 5.5"
          unit="kW"
          helperText="Power required at the drum shaft output."
          errorText={formErrors.powerKw}
        />

        <Input
          keyboardType="decimal-pad"
          label="Required Output Speed"
          value={outputRpm}
          onChangeText={setOutputRpm}
          placeholder="e.g. 70"
          unit="rpm"
          helperText="Rotational speed required at the output shaft."
          errorText={formErrors.outputRpm}
        />

        {submissionState === 'error' && submissionError ? (
          <InlineError message={submissionError} />
        ) : null}
      </Card>

      {/* What happens next */}
      <Card title="What happens" tone="muted">
        <Text variant="bodySmall" tone="secondary">
          The system will select the smallest suitable motor, compute overall efficiency, distribute transmission ratios across belt, bevel gear, and spur gear stages, then calculate power/speed/torque at each shaft.
        </Text>
      </Card>

      {/* Actions */}
      <View style={styles.actions}>
        <Button
          label="Calculate"
          onPress={handleSubmit}
          isLoading={isSubmitting}
          disabled={isSubmitting}
        />
        <Button
          label="Clear"
          onPress={handleReset}
          variant="secondary"
        />
      </View>

      {isSubmitting ? (
        <View pointerEvents="none" style={styles.overlay}>
          <LoadingState
            title="Calculating..."
            description="Selecting motor and computing ratios."
          />
        </View>
      ) : null}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  actions: {
    gap: appTheme.spacing.sm,
  },
  overlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    padding: appTheme.spacing.md,
    backgroundColor: appTheme.colors.overlay,
    borderRadius: appTheme.radii.xl,
  },
});
