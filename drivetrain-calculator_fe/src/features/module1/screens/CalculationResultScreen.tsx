import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';

import { ScreenContainer } from '@/components/layout/ScreenContainer';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';
import { InlineError } from '@/components/ui/InlineError';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { LoadingState } from '@/components/ui/LoadingState';
import { MetricCard } from '@/components/ui/MetricCard';
import { Section } from '@/components/ui/Section';
import { StepIndicator } from '@/components/ui/StepIndicator';
import { Text } from '@/components/ui/Text';
import { routes } from '@/constants/routes';
import { UI_TEXT } from '@/constants/uiText';
import { ShaftDetailCard } from '@/features/module1/components/ShaftDetailCard';
import {
  getCurrentModule1HistoryEntry,
  getModule1HistoryEntryById,
  setCurrentModule1Result,
} from '@/features/module1/state/module1HistoryStore';
import {
  formatPowerKw,
  formatRatio,
  formatRpm,
  formatTorqueNmm,
} from '@/features/module1/utils/formatters';
import { appTheme } from '@/theme';
import type { Module1CalculationResponseDto } from '@/types/api/module1';

type ResultViewState = 'loading' | 'ready' | 'empty' | 'error';

export function CalculationResultScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{ requestId?: string }>();
  const [viewState, setViewState] = useState<ResultViewState>('loading');
  const [result, setResult] = useState<Module1CalculationResponseDto | null>(null);

  useEffect(() => {
    const requestId = typeof params.requestId === 'string' ? params.requestId : undefined;
    const historyEntry = requestId
      ? getModule1HistoryEntryById(requestId)
      : getCurrentModule1HistoryEntry();

    const timerId = setTimeout(() => {
      if (!historyEntry) {
        setViewState(requestId ? 'error' : 'empty');
        return;
      }

      setCurrentModule1Result(historyEntry.id);
      setResult(historyEntry.result);
      setViewState('ready');
    }, 350);

    return () => clearTimeout(timerId);
  }, [params.requestId]);

  if (viewState === 'empty') {
    return (
      <ScreenContainer>
        <EmptyState
          title={UI_TEXT.states.noCalculationResultTitle}
          description={UI_TEXT.states.noCalculationResultDescription}
          actionLabel={UI_TEXT.actions.newCalculation}
          onAction={() => router.replace(routes.calculationsNew)}
        />
      </ScreenContainer>
    );
  }

  if (viewState === 'error') {
    return (
      <ScreenContainer>
        <InlineError message={UI_TEXT.states.unableToCalculate} />
        <Button
          label={UI_TEXT.actions.newCalculation}
          onPress={() => router.replace(routes.calculationsNew)}
        />
      </ScreenContainer>
    );
  }

  if (viewState === 'loading' || !result) {
    return (
      <ScreenContainer>
        <LoadingState
          title={UI_TEXT.states.loadingSavedResultsTitle}
          description={UI_TEXT.states.loadingSavedResultsDescription}
        />
      </ScreenContainer>
    );
  }

  return (
    <ScreenContainer>
      <Section
        eyebrow={`Case #${result.caseInfo.designCaseId}`}
        title="Module 1 Results"
        description="Motor selection and transmission ratio distribution."
      />

      <StepIndicator
        steps={[
          { label: 'Motor', completed: true },
          { label: 'Bevel Gear', active: true },
          { label: 'Spur Gear' },
        ]}
      />

      <View style={styles.metricsRow}>
        <MetricCard
          label="Selected Motor"
          value={result.selectedMotor.motorCode}
          tone="accent"
        />
        <MetricCard
          label="Required Power"
          value={formatPowerKw(result.requiredMotorPowerKw)}
        />
      </View>
      <View style={styles.metricsRow}>
        <MetricCard
          label="Overall Ratio"
          value={formatRatio(result.transmissionRatios.overallRatio)}
        />
        <MetricCard
          label="Efficiency"
          value={formatRatio(result.systemEfficiency)}
        />
      </View>

      <Card
        title={UI_TEXT.results.selectedMotorTitle}
        description={result.selectedMotor.displayName}>
        <KeyValueList
          items={[
            { label: 'Code', value: result.selectedMotor.motorCode, valueVariant: 'bodySmallStrong' },
            { label: 'Manufacturer', value: result.selectedMotor.manufacturer },
            { label: 'Rated Power', value: formatPowerKw(result.selectedMotor.ratedPowerKw) },
            { label: 'Rated Speed', value: formatRpm(result.selectedMotor.ratedRpm) },
          ]}
        />
      </Card>

      <Card
        title={UI_TEXT.results.transmissionRatiosTitle}
        description={UI_TEXT.results.transmissionRatiosDescription}>
        <KeyValueList
          items={[
            { label: UI_TEXT.results.overallRatio, value: formatRatio(result.transmissionRatios.overallRatio) },
            { label: UI_TEXT.results.beltU1, value: formatRatio(result.transmissionRatios.beltRatioU1) },
            { label: UI_TEXT.results.bevelGearU2, value: formatRatio(result.transmissionRatios.bevelRatioU2) },
            { label: UI_TEXT.results.spurGearU3, value: formatRatio(result.transmissionRatios.spurRatioU3) },
          ]}
        />
      </Card>

      <Card
        title={UI_TEXT.results.shaftCharacteristicsTitle}
        description={UI_TEXT.results.shaftCharacteristicsDescription}>
        {result.shaftStates.map((shaft) => (
          <ShaftDetailCard
            key={shaft.shaftCode}
            title={shaft.shaftLabel}
            power={formatPowerKw(shaft.powerKw)}
            rpm={formatRpm(shaft.rpm)}
            torque={formatTorqueNmm(shaft.torqueNmm)}
            summary={shaft.shaftLabel}
          />
        ))}
      </Card>

      {result.calculationNotes.length > 0 ? (
        <Card title={UI_TEXT.results.notesTitle} tone="muted">
          <View style={styles.notesList}>
            {result.calculationNotes.map((note) => (
              <Text key={note} variant="bodySmall" tone="secondary">
                • {note}
              </Text>
            ))}
          </View>
        </Card>
      ) : null}

      <Card tone="accent" title="Next Step" description="Continue to bevel gear design using these results.">
        <Button
          label="Proceed to Bevel Gear Design →"
          onPress={() => {
            router.push({
              pathname: routes.module3New as any,
              params: { designCaseId: result.caseInfo.designCaseId.toString() },
            });
          }}
        />
      </Card>

      <View style={styles.secondaryActions}>
        <Button
          label={UI_TEXT.actions.newCalculation}
          onPress={() => router.replace(routes.calculationsNew)}
          variant="secondary"
        />
        <Button
          label={UI_TEXT.actions.calculationHistory}
          onPress={() => router.push(routes.calculationsHistory)}
          variant="secondary"
        />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  metricsRow: {
    flexDirection: 'row',
    gap: appTheme.spacing.sm,
  },
  notesList: {
    gap: appTheme.spacing.xs,
  },
  secondaryActions: {
    gap: appTheme.spacing.sm,
  },
});
