import { StyleSheet, View } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

type StatusBadgeTone = 'success' | 'error' | 'warning' | 'neutral' | 'info';

type StatusBadgeProps = {
  label: string;
  tone?: StatusBadgeTone;
};

const toneConfig: Record<StatusBadgeTone, { bg: string; border: string; textTone: 'success' | 'error' | 'accent' | 'secondary' }> = {
  success: { bg: appTheme.colors.successSoft, border: '#A8D5B8', textTone: 'success' },
  error: { bg: appTheme.colors.errorSoft, border: '#F2B8B8', textTone: 'error' },
  warning: { bg: appTheme.colors.warningSoft, border: '#E8C97A', textTone: 'error' },
  neutral: { bg: appTheme.colors.cardMuted, border: appTheme.colors.border, textTone: 'secondary' },
  info: { bg: appTheme.colors.primarySoft, border: appTheme.colors.border, textTone: 'accent' },
};

export function StatusBadge({ label, tone = 'neutral' }: StatusBadgeProps) {
  const config = toneConfig[tone];
  return (
    <View style={[styles.base, { backgroundColor: config.bg, borderColor: config.border }]}>
      <Text variant="caption" tone={config.textTone} style={styles.text}>
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
    paddingVertical: 4,
  },
  text: {
    fontWeight: '700',
  },
});
