import { memo } from 'react';
import {
  StyleSheet,
  Text as NativeText,
  type StyleProp,
  type TextProps as NativeTextProps,
  type TextStyle,
} from 'react-native';

import { appTheme } from '@/theme';

type TextVariant =
  | 'eyebrow'
  | 'screenTitle'
  | 'sectionTitle'
  | 'body'
  | 'bodyStrong'
  | 'bodySmall'
  | 'bodySmallStrong'
  | 'caption'
  | 'button'
  | 'mono';

type TextTone =
  | 'primary'
  | 'secondary'
  | 'muted'
  | 'accent'
  | 'error'
  | 'success'
  | 'inverse';

type AppTextProps = NativeTextProps & {
  variant?: TextVariant;
  tone?: TextTone;
  style?: StyleProp<TextStyle>;
};

const toneStyles = StyleSheet.create({
  primary: { color: appTheme.colors.textPrimary },
  secondary: { color: appTheme.colors.textSecondary },
  muted: { color: appTheme.colors.textMuted },
  accent: { color: appTheme.colors.primary },
  error: { color: appTheme.colors.error },
  success: { color: appTheme.colors.success },
  inverse: { color: appTheme.colors.card },
});

const variantStyles = StyleSheet.create({
  eyebrow: appTheme.typography.eyebrow,
  screenTitle: appTheme.typography.screenTitle,
  sectionTitle: appTheme.typography.sectionTitle,
  body: appTheme.typography.body,
  bodyStrong: appTheme.typography.bodyStrong,
  bodySmall: appTheme.typography.bodySmall,
  bodySmallStrong: appTheme.typography.bodySmallStrong,
  caption: appTheme.typography.caption,
  button: appTheme.typography.button,
  mono: appTheme.typography.mono,
});

export const Text = memo(function Text({
  variant = 'body',
  tone = 'primary',
  style,
  ...props
}: AppTextProps) {
  return <NativeText style={[variantStyles[variant], toneStyles[tone], style]} {...props} />;
});
