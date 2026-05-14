import { StyleSheet, View } from 'react-native';

import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';
import type { Module3CalculationResponseDto } from '@/types/api/module3';

type Props = {
  geometry: Module3CalculationResponseDto['gearGeometry'];
};

export function GeometryDetailCard({ geometry }: Props) {
  return (
    <Card
      title={UI_TEXT.module3.geometryTitle}
      description={UI_TEXT.module3.geometryDescription}
    >
      <KeyValueList
        items={[
          { label: UI_TEXT.module3Fields.outerConeDistance, value: `${geometry.reCalculated.toFixed(2)} mm` },
          { label: UI_TEXT.module3Fields.pitchDiameter, value: `${geometry.de1Calculated.toFixed(2)} mm` },
          { label: UI_TEXT.module3Fields.module, value: `${geometry.moduleMteSelected.toFixed(2)}` },
          { label: UI_TEXT.module3Fields.teethZ1, value: `${geometry.teethZ1}` },
          { label: UI_TEXT.module3Fields.teethZ2, value: `${geometry.teethZ2}` },
          { label: UI_TEXT.module3Fields.actualRatio, value: `${geometry.actualRatioU2.toFixed(3)}` },
          { label: UI_TEXT.module3Fields.faceWidth, value: `${geometry.widthBMm.toFixed(2)} mm` },
        ]}
      />
      <View style={styles.divider} />
      <KeyValueList
        items={[
          { label: UI_TEXT.module3Fields.meanDiameter1, value: `${geometry.diameterDm1Mm.toFixed(2)} mm` },
          { label: UI_TEXT.module3Fields.meanDiameter2, value: `${geometry.diameterDm2Mm.toFixed(2)} mm` },
          { label: UI_TEXT.module3Fields.coneAngle1, value: `${geometry.coneAngleDelta1Deg.toFixed(2)}°` },
          { label: UI_TEXT.module3Fields.coneAngle2, value: `${geometry.coneAngleDelta2Deg.toFixed(2)}°` },
        ]}
      />
    </Card>
  );
}

const styles = StyleSheet.create({
  divider: {
    height: 1,
    backgroundColor: appTheme.colors.border,
    marginVertical: appTheme.spacing.sm,
  },
});
