import { StyleSheet, View } from 'react-native';

import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';
import type { Module4CalculationResponseDto } from '@/types/api/module4';

type Props = {
  stress: Module4CalculationResponseDto['stressCheck'];
  input: Module4CalculationResponseDto['inputSummary'];
};

export function SpurStressCheckCard({ stress, input }: Props) {
  return (
    <Card
      title={UI_TEXT.module4.stressesTitle}
      description={UI_TEXT.module4.stressesDescription}
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
            { label: 'σH allowable', value: `${input.allowableContactStressMpa.toFixed(2)} MPa` },
          ]}
        />
      </View>

      <View style={styles.divider} />

      <View style={styles.section}>
        <View style={styles.headerRow}>
          <StatusBadge
            label={stress.bendingStressGear1Pass ? 'GEAR 1 PASS' : 'GEAR 1 FAIL'}
            tone={stress.bendingStressGear1Pass ? 'success' : 'error'}
          />
          <StatusBadge
            label={stress.bendingStressGear2Pass ? 'GEAR 2 PASS' : 'GEAR 2 FAIL'}
            tone={stress.bendingStressGear2Pass ? 'success' : 'error'}
          />
        </View>
        <KeyValueList
          items={[
            { label: 'σF1 actual', value: `${stress.sigmaF1Mpa.toFixed(2)} MPa` },
            { label: 'σF1 allowable', value: `${input.allowableBendingStressGear1Mpa.toFixed(2)} MPa` },
            { label: 'σF2 actual', value: `${stress.sigmaF2Mpa.toFixed(2)} MPa` },
            { label: 'σF2 allowable', value: `${input.allowableBendingStressGear2Mpa.toFixed(2)} MPa` },
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
    gap: appTheme.spacing.xs,
  },
  divider: {
    height: 1,
    backgroundColor: appTheme.colors.border,
    marginVertical: appTheme.spacing.xs,
  },
});
