import type { PropsWithChildren } from 'react';
import { StyleSheet, View, type StyleProp, type ViewStyle } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

type SectionProps = PropsWithChildren<{
  eyebrow?: string;
  title: string;
  description?: string;
  style?: StyleProp<ViewStyle>;
}>;

export function Section({
  eyebrow,
  title,
  description,
  style,
  children,
}: SectionProps) {
  return (
    <View style={[styles.container, style]}>
      {eyebrow ? <Text variant="eyebrow" tone="accent">{eyebrow}</Text> : null}
      <Text variant="screenTitle">{title}</Text>
      {description ? (
        <Text variant="body" tone="secondary">
          {description}
        </Text>
      ) : null}
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: appTheme.spacing.xs,
  },
});
