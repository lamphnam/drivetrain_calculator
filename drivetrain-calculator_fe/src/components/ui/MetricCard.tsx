import { StyleSheet, View } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

type MetricCardProps = {
  label: string;
  value: string;
  unit?: string;
  tone?: 'default' | 'accent' | 'success';
};

export function MetricCard({ label, value, unit, tone = 'default' }: MetricCardProps) {
  return (
    <View style={[styles.base, tone === 'accent' ? styles.accent : null, tone === 'success' ? styles.success : null]}>
      <Text variant="caption" tone="secondary">{label}</Text>
      <View style={styles.valueRow}>
        <Text variant="sectionTitle" tone={tone === 'accent' ? 'accent' : tone === 'success' ? 'success' : 'primary'}>
          {value}
        </Text>
        {unit ? <Text variant="bodySmall" tone="secondary"> {unit}</Text> : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  base: {
    flex: 1,
    minWidth: 100,
    borderRadius: appTheme.radii.md,
    borderWidth: 1,
    borderColor: appTheme.colors.border,
    backgroundColor: appTheme.colors.card,
    padding: appTheme.spacing.sm,
    gap: appTheme.spacing.xxs,
  },
  accent: {
    backgroundColor: appTheme.colors.primarySoft,
    borderColor: appTheme.colors.primary,
  },
  success: {
    backgroundColor: appTheme.colors.successSoft,
    borderColor: '#A8D5B8',
  },
  valueRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
  },
});
