import { StyleSheet, View } from 'react-native';

import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';
import type { Module4CalculationResponseDto } from '@/types/api/module4';

type Props = {
  geometry: Module4CalculationResponseDto['spurGearGeometry'];
};

export function SpurGeometryCard({ geometry }: Props) {
  return (
    <Card
      title={UI_TEXT.module4.geometryTitle}
      description={UI_TEXT.module4.geometryDescription}
    >
      <KeyValueList
        items={[
          { label: UI_TEXT.module4Fields.centerDistance, value: `${geometry.centerDistanceAwMm.toFixed(2)} mm` },
          { label: UI_TEXT.module4Fields.module, value: `${geometry.moduleMSelected.toFixed(2)} mm` },
          { label: UI_TEXT.module4Fields.teethZ1, value: `${geometry.teethZ1}` },
          { label: UI_TEXT.module4Fields.teethZ2, value: `${geometry.teethZ2}` },
          { label: UI_TEXT.module4Fields.actualRatio, value: `${geometry.actualRatioU3.toFixed(4)}` },
          { label: UI_TEXT.module4Fields.ratioError, value: `${geometry.ratioErrorPercent.toFixed(2)} %` },
        ]}
      />
      <View style={styles.divider} />
      <KeyValueList
        items={[
          { label: UI_TEXT.module4Fields.diameterDw1, value: `${geometry.diameterDw1Mm.toFixed(2)} mm` },
          { label: UI_TEXT.module4Fields.diameterDw2, value: `${geometry.diameterDw2Mm.toFixed(2)} mm` },
          { label: UI_TEXT.module4Fields.faceWidth, value: `${geometry.widthBwMm.toFixed(2)} mm` },
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
