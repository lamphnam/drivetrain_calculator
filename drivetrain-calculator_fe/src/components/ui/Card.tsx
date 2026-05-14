import type { PropsWithChildren, ReactNode } from 'react';
import { StyleSheet, View, type StyleProp, type ViewStyle } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

export type CardProps = PropsWithChildren<{
  title?: string;
  description?: string;
  footer?: ReactNode;
  tone?: 'default' | 'muted' | 'accent';
  style?: StyleProp<ViewStyle>;
  bodyStyle?: StyleProp<ViewStyle>;
}>;

export function Card({
  title,
  description,
  footer,
  tone = 'default',
  style,
  bodyStyle,
  children,
}: CardProps) {
  return (
    <View style={[styles.base, tone === 'muted' ? styles.muted : null, tone === 'accent' ? styles.accent : null, style]}>
      {title || description ? (
        <View style={styles.header}>
          {title ? <Text variant="sectionTitle">{title}</Text> : null}
          {description ? (
            <Text variant="body" tone="secondary">
              {description}
            </Text>
          ) : null}
        </View>
      ) : null}

      {children ? <View style={[styles.body, bodyStyle]}>{children}</View> : null}
      {footer ? <View style={styles.footer}>{footer}</View> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  base: {
    gap: appTheme.spacing.md,
    borderRadius: appTheme.radii.lg,
    borderWidth: 1,
    borderColor: appTheme.colors.border,
    backgroundColor: appTheme.colors.card,
    padding: appTheme.spacing.md,
    ...appTheme.elevation.card,
  },
  muted: {
    backgroundColor: appTheme.colors.cardMuted,
  },
  accent: {
    backgroundColor: appTheme.colors.primarySoft,
  },
  header: {
    gap: appTheme.spacing.xs,
  },
  body: {
    gap: appTheme.spacing.sm,
  },
  footer: {
    paddingTop: appTheme.spacing.xs,
  },
});
