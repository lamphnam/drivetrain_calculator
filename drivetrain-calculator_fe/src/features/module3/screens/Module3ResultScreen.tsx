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
import { StatusBadge } from '@/components/ui/StatusBadge';
import { StepIndicator } from '@/components/ui/StepIndicator';
import { Text } from '@/components/ui/Text';
import { routes } from '@/constants/routes';
import { UI_TEXT } from '@/constants/uiText';
import { module3Api } from '@/services/api';
import { GeometryDetailCard } from '@/features/module3/components/GeometryDetailCard';
import { StressCheckCard } from '@/features/module3/components/StressCheckCard';
import { ShaftForcesCard } from '@/features/module3/components/ShaftForcesCard';
import { appTheme } from '@/theme';
import type { Module3CalculationResponseDto } from '@/types/api/module3';

export function Module3ResultScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{ designCaseId: string }>();
  const designCaseId = Number(params.designCaseId);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [result, setResult] = useState<Module3CalculationResponseDto | null>(null);

  useEffect(() => {
    async function loadResult() {
      if (isNaN(designCaseId)) {
        setError('Invalid Design Case ID');
        setIsLoading(false);
        return;
      }

      try {
        const data = await module3Api.getHistory(designCaseId);
        setResult(data);
      } catch {
        setError('Unable to load Module 3 results.');
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
          title="Loading Bevel Gear Results"
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

  const allPass = result.stressCheck.contactStressPass && result.stressCheck.bendingStressPass;

  return (
    <ScreenContainer>
      <Section
        eyebrow={`Case #${result.caseInfo.designCaseId}`}
        title="Bevel Gear Results"
        description="Straight bevel gear geometry and stress verification."
      />

      <StepIndicator
        steps={[
          { label: 'Motor', completed: true },
          { label: 'Bevel Gear', completed: true },
          { label: 'Spur Gear', active: true },
        ]}
      />

      {/* Summary metrics */}
      <View style={styles.metricsRow}>
        <MetricCard
          label="Module (mte)"
          value={`${result.gearGeometry.moduleMteSelected}`}
          unit="mm"
        />
        <MetricCard
          label="Teeth z1/z2"
          value={`${result.gearGeometry.teethZ1}/${result.gearGeometry.teethZ2}`}
        />
      </View>
      <View style={styles.metricsRow}>
        <MetricCard
          label="Actual Ratio"
          value={result.gearGeometry.actualRatioU2.toFixed(3)}
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
            { label: 'Torque T1', value: `${result.inputSummary.inputT1Nmm.toFixed(2)} Nmm` },
            { label: 'Speed n1', value: `${result.inputSummary.inputN1Rpm.toFixed(2)} rpm` },
            { label: 'Ratio u2', value: `${result.inputSummary.inputU2.toFixed(2)}` },
            { label: 'Service Life', value: `${result.inputSummary.serviceLifeHours} h` },
          ]}
        />
      </Card>

      {/* Material */}
      <Card title="Material" description={result.selectedMaterial.materialName}>
        <View style={styles.badgeRow}>
          <StatusBadge label={result.selectedMaterial.materialCode} tone="info" />
          {result.selectedMaterial.heatTreatment ? (
            <StatusBadge label={result.selectedMaterial.heatTreatment} tone="neutral" />
          ) : null}
        </View>
        <KeyValueList
          items={[
            { label: 'Hardness (HB)', value: `${result.selectedMaterial.hbMin} – ${result.selectedMaterial.hbMax}` },
            { label: 'σB', value: `${result.selectedMaterial.sigmaBMpa} MPa` },
            { label: 'σCH', value: `${result.selectedMaterial.sigmaChMpa} MPa` },
          ]}
        />
      </Card>

      <GeometryDetailCard geometry={result.gearGeometry} />

      <StressCheckCard
        stress={result.stressCheck}
        allowable={result.allowableStresses}
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

      {/* Next step */}
      <Card tone="accent" title="Next Step" description="Continue to spur gear design.">
        <Button
          label="Proceed to Spur Gear Design →"
          onPress={() => {
            router.push({
              pathname: routes.module4New as any,
              params: { designCaseId: designCaseId.toString() },
            });
          }}
        />
      </Card>

      <View style={styles.secondaryActions}>
        <Button
          label="Try Different Material"
          onPress={() => {
            router.replace({
              pathname: routes.module3New as any,
              params: { designCaseId: designCaseId.toString() },
            });
          }}
          variant="secondary"
        />
        <Button
          label={UI_TEXT.actions.backToHome}
          onPress={() => router.replace(routes.home)}
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
  badgeRow: {
    flexDirection: 'row',
    gap: appTheme.spacing.xs,
    flexWrap: 'wrap',
  },
  notesList: {
    gap: appTheme.spacing.xs,
  },
  secondaryActions: {
    gap: appTheme.spacing.sm,
  },
});
