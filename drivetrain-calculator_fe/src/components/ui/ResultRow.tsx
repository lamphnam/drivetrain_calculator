import { StyleSheet, View } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

export type ResultRowProps = {
  label: string;
  value: string;
  valueVariant?: 'bodyStrong' | 'bodySmallStrong' | 'mono' | 'sectionTitle';
  valueTone?: 'primary' | 'secondary' | 'muted' | 'accent' | 'error';
};

export function ResultRow({
  label,
  value,
  valueVariant = 'mono',
  valueTone = 'primary',
}: ResultRowProps) {
  return (
    <View style={styles.row}>
      <Text variant="bodySmall" tone="secondary" style={styles.label}>
        {label}
      </Text>
      <Text variant={valueVariant} tone={valueTone} style={styles.value}>
        {value}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: appTheme.spacing.sm,
    paddingVertical: appTheme.spacing.xs,
  },
  label: {
    flex: 1,
  },
  value: {
    flex: 1,
    textAlign: 'right',
  },
});
