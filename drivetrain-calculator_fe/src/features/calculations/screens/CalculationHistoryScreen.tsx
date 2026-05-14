import { useRouter } from 'expo-router';
import { StyleSheet, View } from 'react-native';

import { ScreenContainer } from '@/components/layout/ScreenContainer';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { Section } from '@/components/ui/Section';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { routes } from '@/constants/routes';
import { UI_TEXT } from '@/constants/uiText';
import { useModule1HistoryStore } from '@/features/module1/state/module1HistoryStore';
import { formatDateTime, formatPowerKw, formatRpm } from '@/features/module1/utils/formatters';
import { appTheme } from '@/theme';

export function CalculationHistoryScreen() {
  const router = useRouter();
  const { entries } = useModule1HistoryStore();

  return (
    <ScreenContainer>
      <Section
        eyebrow="History"
        title="Saved Calculations"
        description="Review previous calculations and continue from where you left off."
      />

      {entries.length === 0 ? (
        <EmptyState
          title="No calculations yet"
          description="Run your first calculation to see results here."
          actionLabel="Start New Calculation"
          onAction={() => router.push(routes.calculationsNew)}
        />
      ) : (
        <View style={styles.list}>
          {entries.map((entry) => (
            <Card
              key={entry.id}
              title={`Case #${entry.designCaseId}`}
              description={entry.title}
              footer={
                <Button
                  label="View Results"
                  onPress={() =>
                    router.push({
                      pathname: routes.calculationsResult,
                      params: { requestId: entry.id },
                    })
                  }
                  variant="secondary"
                />
              }>
              <View style={styles.badgeRow}>
                <StatusBadge label={entry.result.caseInfo.status} tone="info" />
              </View>
              <KeyValueList
                items={[
                  { label: 'Power', value: formatPowerKw(entry.result.inputSummary.requiredPowerKw) },
                  { label: 'Output Speed', value: formatRpm(entry.result.inputSummary.requiredOutputRpm) },
                  { label: 'Motor', value: entry.result.selectedMotor.motorCode, valueVariant: 'bodySmallStrong' },
                  { label: 'Calculated', value: formatDateTime(entry.createdAt), valueVariant: 'bodySmallStrong' },
                ]}
              />
            </Card>
          ))}
        </View>
      )}

      <View style={styles.actions}>
        <Button
          label="Start New Calculation"
          onPress={() => router.push(routes.calculationsNew)}
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
  list: {
    gap: appTheme.spacing.sm,
  },
  badgeRow: {
    flexDirection: 'row',
    gap: appTheme.spacing.xs,
  },
  actions: {
    gap: appTheme.spacing.sm,
  },
});
