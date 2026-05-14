import { useLocalSearchParams, useRouter } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';

import { ScreenContainer } from '@/components/layout/ScreenContainer';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { InheritedField } from '@/components/ui/InheritedField';
import { InlineError } from '@/components/ui/InlineError';
import { Input } from '@/components/ui/Input';
import { Section } from '@/components/ui/Section';
import { StepIndicator } from '@/components/ui/StepIndicator';
import { Text } from '@/components/ui/Text';
import { routes } from '@/constants/routes';
import { UI_TEXT } from '@/constants/uiText';
import { useModule4Calculation } from '@/features/module4/hooks/useModule4Calculation';
import { appTheme } from '@/theme';

export function NewModule4CalculationScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{ designCaseId: string }>();
  const designCaseId = Number(params.designCaseId);

  const [showOverrides, setShowOverrides] = useState(false);
  const [inputT2, setInputT2] = useState('');
  const [inputN2, setInputN2] = useState('');
  const [inputU3, setInputU3] = useState('');
  const [sigmaH, setSigmaH] = useState('');
  const [sigmaF1, setSigmaF1] = useState('');
  const [sigmaF2, setSigmaF2] = useState('');

  const {
    isSubmitting,
    submissionError,
    submitCalculation,
  } = useModule4Calculation();

  async function handleSubmit() {
    if (isNaN(designCaseId)) return;

    const result = await submitCalculation({
      designCaseId,
      inputT2Nmm: inputT2 ? Number(inputT2) : undefined,
      inputN2Rpm: inputN2 ? Number(inputN2) : undefined,
      inputU3: inputU3 ? Number(inputU3) : undefined,
      allowableContactStressMpa: sigmaH ? Number(sigmaH) : undefined,
      allowableBendingStressGear1Mpa: sigmaF1 ? Number(sigmaF1) : undefined,
      allowableBendingStressGear2Mpa: sigmaF2 ? Number(sigmaF2) : undefined,
    });

    if (result.result) {
      router.push({
        pathname: routes.module4Result as any,
        params: { designCaseId: designCaseId.toString() },
      });
    }
  }

  return (
    <ScreenContainer>
      <Section
        eyebrow={`Case #${designCaseId}`}
        title="Spur Gear Design"
        description="Input values are automatically inherited from Module 1. Stress limits cascade from Module 3 or use defaults."
      />

      <StepIndicator
        steps={[
          { label: 'Motor', completed: true },
          { label: 'Bevel Gear', completed: true },
          { label: 'Spur Gear', active: true },
        ]}
      />

      <Card title="Input Values" description="Automatically resolved from your previous module results.">
        <InheritedField
          label="Torque T2"
          inheritedValue="From Module 1 SHAFT_2"
          unit="Nmm"
          source="Module 1 shaft state"
        />
        <InheritedField
          label="Speed n2"
          inheritedValue="From Module 1 SHAFT_2"
          unit="rpm"
          source="Module 1 shaft state"
        />
        <InheritedField
          label="Spur Ratio u3"
          inheritedValue="From Module 1"
          source="Module 1 transmission ratios"
        />
      </Card>

      <Card title="Stress Limits" description="Cascaded from Module 3 results or system defaults.">
        <InheritedField
          label="Allowable Contact (σH)"
          inheritedValue="From Module 3 or default 600"
          unit="MPa"
          source="Module 3 / system default"
        />
        <InheritedField
          label="Allowable Bending (σF1)"
          inheritedValue="From Module 3 or default 260"
          unit="MPa"
          source="Module 3 / system default"
        />
        <InheritedField
          label="Allowable Bending (σF2)"
          inheritedValue="From Module 3 or default 260"
          unit="MPa"
          source="Module 3 / system default"
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
            Only fill these if you want to override the auto-inherited values.
          </Text>
          <Input
            label="Torque T2"
            value={inputT2}
            onChangeText={setInputT2}
            keyboardType="decimal-pad"
            unit="Nmm"
            placeholder="Leave blank for auto"
          />
          <Input
            label="Speed n2"
            value={inputN2}
            onChangeText={setInputN2}
            keyboardType="decimal-pad"
            unit="rpm"
            placeholder="Leave blank for auto"
          />
          <Input
            label="Spur Ratio u3"
            value={inputU3}
            onChangeText={setInputU3}
            keyboardType="decimal-pad"
            placeholder="Leave blank for auto"
          />
          <Input
            label="Allowable Contact σH"
            value={sigmaH}
            onChangeText={setSigmaH}
            keyboardType="decimal-pad"
            unit="MPa"
            placeholder="Leave blank for auto"
          />
          <Input
            label="Allowable Bending σF1"
            value={sigmaF1}
            onChangeText={setSigmaF1}
            keyboardType="decimal-pad"
            unit="MPa"
            placeholder="Leave blank for auto"
          />
          <Input
            label="Allowable Bending σF2"
            value={sigmaF2}
            onChangeText={setSigmaF2}
            keyboardType="decimal-pad"
            unit="MPa"
            placeholder="Leave blank for auto"
          />
        </Card>
      ) : null}

      <Card tone="muted">
        <Text variant="bodySmall" tone="secondary">
          The system will compute center distance, select a standard module (1–12 mm), determine tooth counts, and verify contact and bending stress for both gears.
        </Text>
      </Card>

      {submissionError ? <InlineError message={submissionError} /> : null}

      <View style={styles.actions}>
        <Button
          label="Calculate Spur Gear"
          onPress={handleSubmit}
          isLoading={isSubmitting}
          disabled={isSubmitting}
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
