import { StyleSheet, View } from 'react-native';

import { ScreenContainer } from '@/components/layout/ScreenContainer';
import { Badge } from '@/components/ui/Badge';
import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { Section } from '@/components/ui/Section';
import { Text } from '@/components/ui/Text';
import { ACTIVE_MODULE, APP_NAME } from '@/constants/app';
import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';

export function SettingsScreen() {
  return (
    <ScreenContainer>
      <View style={styles.badges}>
        <Badge label={ACTIVE_MODULE} />
        <Badge label={UI_TEXT.badge.calculator} />
      </View>

      <Section
        title={UI_TEXT.settings.title}
        description={UI_TEXT.settings.description}
      />

      <Card
        title={UI_TEXT.settings.appInformationTitle}
        description={UI_TEXT.settings.appInformationDescription}>
        <KeyValueList
          items={[
            { label: UI_TEXT.settings.appName, value: APP_NAME, valueVariant: 'bodySmallStrong' },
            { label: 'Version', value: '1.0.0', valueVariant: 'bodySmallStrong' },
            { label: UI_TEXT.settings.activeModule, value: ACTIVE_MODULE, valueVariant: 'bodySmallStrong' },
            {
              label: UI_TEXT.settings.calculationMode,
              value: UI_TEXT.settings.calculationModeValue,
              valueVariant: 'bodySmallStrong',
            },
            {
              label: UI_TEXT.settings.dataSource,
              value: UI_TEXT.settings.dataSourceValue,
              valueVariant: 'bodySmallStrong',
            },
            {
              label: UI_TEXT.settings.reference,
              value: UI_TEXT.settings.referenceValue,
              valueVariant: 'bodySmallStrong',
            },
          ]}
        />
      </Card>

      <Card
        title={UI_TEXT.settings.displayPreferencesTitle}
        description={UI_TEXT.settings.displayPreferencesDescription}>
        <KeyValueList
          items={[
            { label: UI_TEXT.settings.units, value: UI_TEXT.settings.unitsValue, valueVariant: 'bodySmallStrong' },
            {
              label: UI_TEXT.settings.numericFormatting,
              value: UI_TEXT.settings.numericFormattingValue,
              valueVariant: 'bodySmallStrong',
            },
            {
              label: UI_TEXT.settings.environment,
              value: UI_TEXT.settings.environmentValue,
              valueVariant: 'bodySmallStrong',
            },
          ]}
        />
      </Card>

      <Card
        title={UI_TEXT.settings.currentScopeTitle}
        description={UI_TEXT.settings.currentScopeDescription}>
        <Text variant="body">- {UI_TEXT.settings.currentScopeBullets[0]}</Text>
        <Text variant="body">- {UI_TEXT.settings.currentScopeBullets[1]}</Text>
        <Text variant="body">- {UI_TEXT.settings.currentScopeBullets[2]}</Text>
      </Card>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  badges: {
    flexDirection: 'row',
    gap: appTheme.spacing.sm,
    flexWrap: 'wrap',
  },
});
