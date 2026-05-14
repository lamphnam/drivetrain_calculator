import { StyleSheet, View } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';
import { StatusBadge } from './StatusBadge';

type InheritedFieldProps = {
  label: string;
  inheritedValue: string;
  unit?: string;
  source?: string;
};

export function InheritedField({ label, inheritedValue, unit, source }: InheritedFieldProps) {
  return (
    <View style={styles.container}>
      <View style={styles.labelRow}>
        <Text variant="bodySmallStrong">{label}</Text>
        <StatusBadge label="AUTO" tone="info" />
      </View>
      <View style={styles.valueRow}>
        <Text variant="mono" tone="primary">
          {inheritedValue}{unit ? ` ${unit}` : ''}
        </Text>
      </View>
      {source ? (
        <Text variant="caption" tone="muted">Inherited from {source}</Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: appTheme.spacing.xxs,
    paddingVertical: appTheme.spacing.xs,
    paddingHorizontal: appTheme.spacing.sm,
    borderRadius: appTheme.radii.md,
    backgroundColor: appTheme.colors.backgroundElevated,
    borderWidth: 1,
    borderColor: appTheme.colors.border,
  },
  labelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  valueRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
  },
});
