import { StyleSheet, View, type StyleProp, type ViewStyle } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

type BadgeTone = 'info' | 'warning' | 'neutral';

type BadgeProps = {
  label: string;
  tone?: BadgeTone;
  style?: StyleProp<ViewStyle>;
};

export function Badge({ label, tone = 'info', style }: BadgeProps) {
  return (
    <View style={[styles.base, styles[tone], style]}>
      <Text variant="caption" tone={tone === 'warning' ? 'error' : tone === 'neutral' ? 'secondary' : 'accent'}>
        {label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  base: {
    alignSelf: 'flex-start',
    borderRadius: appTheme.radii.pill,
    borderWidth: 1,
    paddingHorizontal: appTheme.spacing.sm,
    paddingVertical: 6,
  },
  info: {
    backgroundColor: appTheme.colors.primarySoft,
    borderColor: appTheme.colors.border,
  },
  warning: {
    backgroundColor: appTheme.colors.errorSoft,
    borderColor: '#E7C1C1',
  },
  neutral: {
    backgroundColor: appTheme.colors.cardMuted,
    borderColor: appTheme.colors.border,
  },
});
