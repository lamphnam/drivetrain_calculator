import { StyleSheet, View } from 'react-native';

import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';
import type { Module3CalculationResponseDto } from '@/types/api/module3';

type Props = {
  stress: Module3CalculationResponseDto['stressCheck'];
  allowable: Module3CalculationResponseDto['allowableStresses'];
};

export function StressCheckCard({ stress, allowable }: Props) {
  return (
    <Card
      title={UI_TEXT.module3.stressesTitle}
      description={UI_TEXT.module3.stressesDescription}
    >
      <View style={styles.section}>
        <View style={styles.headerRow}>
          <StatusBadge
            label={stress.contactStressPass ? 'CONTACT PASS' : 'CONTACT FAIL'}
            tone={stress.contactStressPass ? 'success' : 'error'}
          />
        </View>
        <KeyValueList
          items={[
            { label: 'σH actual', value: `${stress.sigmaHMpa.toFixed(2)} MPa` },
            { label: 'σH allowable', value: `${allowable.allowableContactStressMpa.toFixed(2)} MPa` },
          ]}
        />
      </View>

      <View style={styles.divider} />

      <View style={styles.section}>
        <View style={styles.headerRow}>
          <StatusBadge
            label={stress.bendingStressPass ? 'BENDING PASS' : 'BENDING FAIL'}
            tone={stress.bendingStressPass ? 'success' : 'error'}
          />
        </View>
        <KeyValueList
          items={[
            { label: 'σF1 actual', value: `${stress.sigmaF1Mpa.toFixed(2)} MPa` },
            { label: 'σF2 actual', value: `${stress.sigmaF2Mpa.toFixed(2)} MPa` },
            { label: 'σF allowable', value: `${allowable.allowableBendingStressMpa.toFixed(2)} MPa` },
          ]}
        />
      </View>
    </Card>
  );
}

const styles = StyleSheet.create({
  section: {
    gap: appTheme.spacing.sm,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  divider: {
    height: 1,
    backgroundColor: appTheme.colors.border,
    marginVertical: appTheme.spacing.xs,
  },
});
