import { StyleSheet, View } from 'react-native';

import { appTheme } from '@/theme';
import { Button } from './Button';
import { Text } from './Text';

type EmptyStateProps = {
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
};

export function EmptyState({
  title,
  description,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  return (
    <View style={styles.container}>
      <View style={styles.illustration}>
        <View style={styles.circle} />
        <View style={styles.line} />
      </View>
      <Text variant="sectionTitle">{title}</Text>
      <Text variant="body" tone="secondary">
        {description}
      </Text>
      {actionLabel && onAction ? (
        <View style={styles.action}>
          <Button label={actionLabel} onPress={onAction} variant="secondary" />
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    borderRadius: appTheme.radii.lg,
    padding: appTheme.spacing.lg,
    backgroundColor: appTheme.colors.cardMuted,
    borderWidth: 1,
    borderColor: appTheme.colors.border,
    gap: appTheme.spacing.sm,
  },
  illustration: {
    width: 56,
    alignItems: 'center',
    gap: 10,
  },
  circle: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: appTheme.colors.primarySoft,
  },
  line: {
    width: 28,
    height: 4,
    borderRadius: appTheme.radii.pill,
    backgroundColor: appTheme.colors.primary,
  },
  action: {
    paddingTop: appTheme.spacing.xs,
  },
});
