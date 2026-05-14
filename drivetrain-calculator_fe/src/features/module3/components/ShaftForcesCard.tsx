import { StyleSheet, View } from 'react-native';

import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { Text } from '@/components/ui/Text';
import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';
import type { ShaftForceDto } from '@/types/api/common';

type Props = {
  forces: ShaftForceDto[];
};

export function ShaftForcesCard({ forces }: Props) {
  return (
    <Card
      title={UI_TEXT.module3.forcesTitle}
      description={UI_TEXT.module3.forcesDescription}
    >
      {forces.map((shaft, index) => (
        <View key={shaft.shaftCode} style={index > 0 ? styles.marginTop : undefined}>
          <Text variant="bodyStrong" style={styles.shaftTitle}>{shaft.shaftLabel}</Text>
          <KeyValueList
            items={[
              { label: UI_TEXT.module3Fields.tangentialForce, value: `${shaft.ftN.toFixed(2)} N` },
              { label: UI_TEXT.module3Fields.radialForce, value: `${shaft.frN.toFixed(2)} N` },
              { label: UI_TEXT.module3Fields.axialForce, value: `${shaft.faN.toFixed(2)} N` },
            ]}
          />
          {index < forces.length - 1 && <View style={styles.divider} />}
        </View>
      ))}
    </Card>
  );
}

const styles = StyleSheet.create({
  shaftTitle: {
    marginBottom: appTheme.spacing.xs,
  },
  marginTop: {
    marginTop: appTheme.spacing.sm,
  },
  divider: {
    height: 1,
    backgroundColor: appTheme.colors.border,
    marginVertical: appTheme.spacing.md,
  },
});
