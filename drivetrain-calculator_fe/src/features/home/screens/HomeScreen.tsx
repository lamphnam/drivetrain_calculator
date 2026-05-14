import { useRouter } from 'expo-router';
import { StyleSheet, View } from 'react-native';

import { ScreenContainer } from '@/components/layout/ScreenContainer';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { Section } from '@/components/ui/Section';
import { StepIndicator } from '@/components/ui/StepIndicator';
import { Text } from '@/components/ui/Text';
import { routes } from '@/constants/routes';
import { UI_TEXT } from '@/constants/uiText';
import { useModule1HistoryStore } from '@/features/module1/state/module1HistoryStore';
import { formatDateTime, formatPowerKw, formatRpm } from '@/features/module1/utils/formatters';
import { appTheme } from '@/theme';

export function HomeScreen() {
  const router = useRouter();
  const { entries } = useModule1HistoryStore();
  const latestEntry = entries[0];

  return (
    <ScreenContainer>
      <Section
        eyebrow="Drivetrain Calculator"
        title="Mixing Drum Drive Design"
        description="Design the complete power transmission chain: Motor → Belt → Bevel Gear → Spur Gear → Drum."
      />

      <Card title="Design Flow">
        <StepIndicator
          steps={[
            { label: 'Motor', active: true },
            { label: 'Bevel Gear' },
            { label: 'Spur Gear' },
          ]}
        />
        <Text variant="bodySmall" tone="secondary">
          Start with motor selection, then proceed through gear design stages sequentially.
        </Text>
      </Card>

      <View style={styles.primaryAction}>
        <Button
          label="Start New Calculation"
          onPress={() => router.push(routes.calculationsNew)}
        />
      </View>

      {latestEntry ? (
        <Card
          title="Latest Calculation"
          description={`Case #${latestEntry.designCaseId}`}
          footer={
            <Button
              label="View Results"
              onPress={() =>
                router.push({
                  pathname: routes.calculationsResult,
                  params: { requestId: latestEntry.id },
                })
              }
              variant="secondary"
            />
          }>
          <KeyValueList
            items={[
              {
                label: 'Motor',
                value: latestEntry.result.selectedMotor.motorCode,
                valueVariant: 'bodySmallStrong',
              },
              {
                label: 'Power',
                value: formatPowerKw(latestEntry.result.inputSummary.requiredPowerKw),
              },
              {
                label: 'Output Speed',
                value: formatRpm(latestEntry.result.inputSummary.requiredOutputRpm),
              },
              {
                label: 'Calculated',
                value: formatDateTime(latestEntry.createdAt),
                valueVariant: 'bodySmallStrong',
              },
            ]}
          />
        </Card>
      ) : null}

      <View style={styles.secondaryActions}>
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
  primaryAction: {
    gap: appTheme.spacing.sm,
  },
  secondaryActions: {
    gap: appTheme.spacing.sm,
  },
});
