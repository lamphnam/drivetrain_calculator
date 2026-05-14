import { StyleSheet, View } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

type Step = {
  label: string;
  active?: boolean;
  completed?: boolean;
};

type StepIndicatorProps = {
  steps: Step[];
};

export function StepIndicator({ steps }: StepIndicatorProps) {
  return (
    <View style={styles.container}>
      {steps.map((step, index) => (
        <View key={step.label} style={styles.stepRow}>
          <View style={[styles.dot, step.completed ? styles.dotCompleted : null, step.active ? styles.dotActive : null]}>
            {step.completed ? (
              <Text variant="caption" tone="inverse">✓</Text>
            ) : (
              <Text variant="caption" tone={step.active ? 'inverse' : 'secondary'}>{index + 1}</Text>
            )}
          </View>
          <Text
            variant={step.active ? 'bodySmallStrong' : 'bodySmall'}
            tone={step.active ? 'primary' : step.completed ? 'success' : 'muted'}
          >
            {step.label}
          </Text>
          {index < steps.length - 1 ? <View style={styles.connector} /> : null}
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: appTheme.spacing.xxs,
  },
  stepRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: appTheme.spacing.xs,
  },
  dot: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: appTheme.colors.cardMuted,
    borderWidth: 1,
    borderColor: appTheme.colors.border,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dotActive: {
    backgroundColor: appTheme.colors.primary,
    borderColor: appTheme.colors.primary,
  },
  dotCompleted: {
    backgroundColor: appTheme.colors.success,
    borderColor: appTheme.colors.success,
  },
  connector: {
    width: 12,
    height: 2,
    backgroundColor: appTheme.colors.border,
    borderRadius: 1,
  },
});
