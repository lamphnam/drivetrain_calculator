import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';

import { ScreenContainer } from '@/components/layout/ScreenContainer';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { InlineError } from '@/components/ui/InlineError';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { LoadingState } from '@/components/ui/LoadingState';
import { MetricCard } from '@/components/ui/MetricCard';
import { Section } from '@/components/ui/Section';
import { StepIndicator } from '@/components/ui/StepIndicator';
import { Text } from '@/components/ui/Text';
import { routes } from '@/constants/routes';
import { UI_TEXT } from '@/constants/uiText';
import { module4Api } from '@/services/api';
import { SpurGeometryCard } from '@/features/module4/components/SpurGeometryCard';
import { SpurStressCheckCard } from '@/features/module4/components/SpurStressCheckCard';
import { DerivedFactorsCard } from '@/features/module4/components/DerivedFactorsCard';
import { ShaftForcesCard } from '@/features/module3/components/ShaftForcesCard';
import { appTheme } from '@/theme';
import type { Module4CalculationResponseDto } from '@/types/api/module4';

export function Module4ResultScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{ designCaseId: string }>();
  const designCaseId = Number(params.designCaseId);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [result, setResult] = useState<Module4CalculationResponseDto | null>(null);

  useEffect(() => {
    async function loadResult() {
      if (isNaN(designCaseId)) {
        setError('Invalid Design Case ID');
        setIsLoading(false);
        return;
      }

      try {
        const data = await module4Api.getHistory(designCaseId);
        setResult(data);
      } catch {
        setError('Unable to load Module 4 results.');
      } finally {
        setIsLoading(false);
      }
    }

    loadResult();
  }, [designCaseId]);

  if (isLoading) {
    return (
      <ScreenContainer>
        <LoadingState
          title="Loading Spur Gear Results"
          description="Fetching calculation data..."
        />
      </ScreenContainer>
    );
  }

  if (error || !result) {
    return (
      <ScreenContainer>
        <InlineError message={error || 'No result found.'} />
        <Button
          label={UI_TEXT.actions.backToHome}
          onPress={() => router.replace(routes.home)}
        />
      </ScreenContainer>
    );
  }

  const allPass = result.stressCheck.contactStressPass &&
    result.stressCheck.bendingStressGear1Pass &&
    result.stressCheck.bendingStressGear2Pass;

  return (
    <ScreenContainer>
      <Section
        eyebrow={`Case #${result.caseInfo.designCaseId}`}
        title="Spur Gear Results"
        description="Straight spur gear geometry and stress verification."
      />

      <StepIndicator
        steps={[
          { label: 'Motor', completed: true },
          { label: 'Bevel Gear', completed: true },
          { label: 'Spur Gear', completed: true },
        ]}
      />

      {/* Summary metrics */}
      <View style={styles.metricsRow}>
        <MetricCard
          label="Module (m)"
          value={`${result.spurGearGeometry.moduleMSelected}`}
          unit="mm"
        />
        <MetricCard
          label="Teeth z1/z2"
          value={`${result.spurGearGeometry.teethZ1}/${result.spurGearGeometry.teethZ2}`}
        />
      </View>
      <View style={styles.metricsRow}>
        <MetricCard
          label="Center Distance"
          value={`${result.spurGearGeometry.centerDistanceAwMm.toFixed(2)}`}
          unit="mm"
        />
        <MetricCard
          label="Stress Check"
          value={allPass ? 'ALL PASS' : 'FAIL'}
          tone={allPass ? 'success' : 'default'}
        />
      </View>

      {/* Input summary */}
      <Card title="Input Parameters">
        <KeyValueList
          items={[
            { label: 'Torque T2', value: `${result.inputSummary.inputT2Nmm.toFixed(2)} Nmm` },
            { label: 'Speed n2', value: `${result.inputSummary.inputN2Rpm.toFixed(2)} rpm` },
            { label: 'Ratio u3', value: `${result.inputSummary.inputU3.toFixed(2)}` },
          ]}
        />
      </Card>

      <SpurGeometryCard geometry={result.spurGearGeometry} />

      <DerivedFactorsCard factors={result.derivedFactors} />

      <SpurStressCheckCard
        stress={result.stressCheck}
        input={result.inputSummary}
      />

      <ShaftForcesCard forces={result.shaftForces} />

      {/* Notes */}
      {result.calculationNotes.length > 0 ? (
        <Card title="Calculation Notes" tone="muted">
          <View style={styles.notesList}>
            {result.calculationNotes.map((note, i) => (
              <Text key={i} variant="bodySmall" tone="secondary">• {note}</Text>
            ))}
          </View>
        </Card>
      ) : null}

      {/* Completion */}
      <Card tone="accent" title="Design Complete" description="All three modules have been calculated successfully.">
        <MetricCard
          label="Final Status"
          value={allPass ? 'All Checks Passed' : 'Review Required'}
          tone={allPass ? 'success' : 'default'}
        />
      </Card>

      <View style={styles.secondaryActions}>
        <Button
          label="Start New Design"
          onPress={() => router.replace(routes.calculationsNew)}
        />
        <Button
          label={UI_TEXT.actions.backToHome}
          onPress={() => router.replace(routes.home)}
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
