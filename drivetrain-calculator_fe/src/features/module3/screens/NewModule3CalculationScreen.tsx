import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';

import { ScreenContainer } from '@/components/layout/ScreenContainer';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { InheritedField } from '@/components/ui/InheritedField';
import { InlineError } from '@/components/ui/InlineError';
import { Input } from '@/components/ui/Input';
import { LoadingState } from '@/components/ui/LoadingState';
import { Section } from '@/components/ui/Section';
import { StepIndicator } from '@/components/ui/StepIndicator';
import { Text } from '@/components/ui/Text';
import { routes } from '@/constants/routes';
import { UI_TEXT } from '@/constants/uiText';
import { useModule3Calculation } from '@/features/module3/hooks/useModule3Calculation';
import { MaterialSelectionCard } from '@/features/module3/components/MaterialSelectionCard';
import { appTheme } from '@/theme';

export function NewModule3CalculationScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{ designCaseId: string }>();
  const designCaseId = Number(params.designCaseId);

  const [materialId, setMaterialId] = useState<number | null>(null);
  const [showOverrides, setShowOverrides] = useState(false);
  const [inputT1, setInputT1] = useState('');
  const [inputN1, setInputN1] = useState('');
  const [inputU2, setInputU2] = useState('');
  const [lifeHours, setLifeHours] = useState('');

  const {
    bootstrapData,
    bootstrapError,
    isBootstrapping,
    isSubmitting,
    submissionError,
    loadBootstrapData,
    submitCalculation,
  } = useModule3Calculation();

  useEffect(() => {
    loadBootstrapData();
  }, [loadBootstrapData]);

  async function handleSubmit() {
    if (!materialId || isNaN(designCaseId)) return;

    const result = await submitCalculation({
      designCaseId,
      materialId,
      inputT1Nmm: inputT1 ? Number(inputT1) : undefined,
      inputN1Rpm: inputN1 ? Number(inputN1) : undefined,
      inputU2: inputU2 ? Number(inputU2) : undefined,
      serviceLifeHours: lifeHours ? Number(lifeHours) : undefined,
    });

    if (result.result) {
      router.push({
        pathname: routes.module3Result as any,
        params: { designCaseId: designCaseId.toString() },
      });
    }
  }

  if (isBootstrapping) {
    return (
      <ScreenContainer>
        <LoadingState
          title="Loading Materials"
          description="Fetching available gear materials..."
        />
      </ScreenContainer>
    );
  }

  return (
    <ScreenContainer>
      <Section
        eyebrow={`Case #${designCaseId}`}
        title="Bevel Gear Design"
        description="Select a gear material. Input values are automatically inherited from Module 1."
      />

      <StepIndicator
        steps={[
          { label: 'Motor', completed: true },
          { label: 'Bevel Gear', active: true },
          { label: 'Spur Gear' },
        ]}
      />

      {bootstrapError ? <InlineError message={bootstrapError} /> : null}

      <MaterialSelectionCard
        materials={bootstrapData.materials}
        selectedId={materialId}
        onSelect={setMaterialId}
      />

      <Card title="Input Values" description="These values are automatically resolved from your Module 1 results.">
        <InheritedField
          label="Torque T1"
          inheritedValue="From Module 1 SHAFT_1"
          unit="Nmm"
          source="Module 1 shaft state"
        />
        <InheritedField
          label="Speed n1"
          inheritedValue="From Module 1 SHAFT_1"
          unit="rpm"
          source="Module 1 shaft state"
        />
        <InheritedField
          label="Bevel Ratio u2"
          inheritedValue="≈ 3.14"
          source="Module 1 transmission ratios"
        />
        <InheritedField
          label="Service Life"
          inheritedValue="43,200"
          unit="h"
          source="System default"
        />
      </Card>

      <Pressable onPress={() => setShowOverrides(!showOverrides)} style={styles.toggleRow}>
        <Text variant="bodySmallStrong" tone="accent">
          {showOverrides ? '▾ Hide manual overrides' : '▸ Override values manually'}
        </Text>
      </Pressable>

      {showOverrides ? (
        <Card title="Manual Overrides" tone="muted">
          <Text variant="caption" tone="secondary">
            Only fill these if you want to override the auto-inherited values above.
          </Text>
          <Input
            label="Torque T1"
            value={inputT1}
            onChangeText={setInputT1}
            keyboardType="decimal-pad"
            unit="Nmm"
            placeholder="Leave blank for auto"
          />
          <Input
            label="Speed n1"
            value={inputN1}
            onChangeText={setInputN1}
            keyboardType="decimal-pad"
            unit="rpm"
            placeholder="Leave blank for auto"
          />
          <Input
            label="Bevel Ratio u2"
            value={inputU2}
            onChangeText={setInputU2}
            keyboardType="decimal-pad"
            placeholder="Leave blank for auto"
          />
          <Input
            label="Service Life"
            value={lifeHours}
            onChangeText={setLifeHours}
            keyboardType="decimal-pad"
            unit="h"
            placeholder="Leave blank for 43,200 h"
          />
        </Card>
      ) : null}

      <Card tone="muted">
        <Text variant="bodySmall" tone="secondary">
          The system will select a standard module (2–12 mm), compute tooth counts, gear dimensions, and verify contact/bending stress against the selected material allowable limits.
        </Text>
      </Card>

      {submissionError ? <InlineError message={submissionError} /> : null}

      <View style={styles.actions}>
        <Button
          label="Calculate Bevel Gear"
          onPress={handleSubmit}
          isLoading={isSubmitting}
          disabled={!materialId || isSubmitting}
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
  toggleRow: {
    paddingVertical: appTheme.spacing.sm,
    paddingHorizontal: appTheme.spacing.xs,
  },
  actions: {
    gap: appTheme.spacing.sm,
    marginTop: appTheme.spacing.xs,
  },
});
