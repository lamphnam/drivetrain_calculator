import { StyleSheet, View } from 'react-native';

import { appTheme } from '@/theme';
import { ResultRow } from './ResultRow';

export type KeyValueItem = {
  label: string;
  value: string;
  valueVariant?: 'bodyStrong' | 'bodySmallStrong' | 'mono' | 'sectionTitle';
  valueTone?: 'primary' | 'secondary' | 'muted' | 'accent' | 'error';
};

type KeyValueListProps = {
  items: KeyValueItem[];
};

export function KeyValueList({ items }: KeyValueListProps) {
  return (
    <View style={styles.list}>
      {items.map((item) => (
        <ResultRow
          key={item.label}
          label={item.label}
          value={item.value}
          valueVariant={item.valueVariant}
          valueTone={item.valueTone}
        />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  list: {
    gap: appTheme.spacing.xs,
  },
});
