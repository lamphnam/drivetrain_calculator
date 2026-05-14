import { StyleSheet, TouchableOpacity, View } from 'react-native';

import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { Text } from '@/components/ui/Text';
import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';
import type { Module3MaterialDto } from '@/types/api/module3';

type Props = {
  materials: Module3MaterialDto[];
  selectedId: number | null;
  onSelect: (id: number) => void;
};

export function MaterialSelectionCard({ materials, selectedId, onSelect }: Props) {
  return (
    <Card
      title={UI_TEXT.module3.materialsTitle}
      description={UI_TEXT.module3.materialsDescription}
    >
      <View style={styles.list}>
        {materials.map((item) => {
          const isSelected = item.materialId === selectedId;
          return (
            <TouchableOpacity
              key={item.materialId}
              onPress={() => onSelect(item.materialId)}
              style={[
                styles.item,
                isSelected && styles.itemSelected,
              ]}
            >
              <View style={styles.header}>
                <Text variant={isSelected ? 'bodyStrong' : 'body'}>
                  {item.materialName} ({item.materialCode})
                </Text>
                {isSelected && <View style={styles.radioActive} />}
                {!isSelected && <View style={styles.radioInactive} />}
              </View>

              {isSelected && (
                <View style={styles.details}>
                  <KeyValueList
                    items={[
                      { label: UI_TEXT.module3Fields.heatTreatment, value: item.heatTreatment || '-' },
                      { label: UI_TEXT.module3Fields.hardness, value: `${item.hbMin} - ${item.hbMax}` },
                      { label: UI_TEXT.module3Fields.tensileStrength, value: `${item.sigmaBMpa} MPa` },
                      { label: UI_TEXT.module3Fields.yieldStrength, value: `${item.sigmaChMpa} MPa` },
                    ]}
                  />
                </View>
              )}
            </TouchableOpacity>
          );
        })}
      </View>
    </Card>
  );
}

const styles = StyleSheet.create({
  list: {
    gap: appTheme.spacing.sm,
  },
  item: {
    padding: appTheme.spacing.md,
    borderRadius: appTheme.radii.lg,
    borderWidth: 1,
    borderColor: appTheme.colors.border,
    backgroundColor: appTheme.colors.cardMuted,
  },
  itemSelected: {
    borderColor: appTheme.colors.primary,
    backgroundColor: appTheme.colors.primarySoft,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  details: {
    marginTop: appTheme.spacing.md,
    paddingTop: appTheme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: appTheme.colors.border,
  },
  radioActive: {
    width: 16,
    height: 16,
    borderRadius: 8,
    borderWidth: 5,
    borderColor: appTheme.colors.primary,
  },
  radioInactive: {
    width: 16,
    height: 16,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: appTheme.colors.border,
  },
});
