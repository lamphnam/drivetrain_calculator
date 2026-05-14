import { ActivityIndicator, StyleSheet } from 'react-native';

import { appTheme } from '@/theme';
import { Card } from './Card';
import { Text } from './Text';

type LoadingStateProps = {
  title: string;
  description?: string;
};

export function LoadingState({ title, description }: LoadingStateProps) {
  return (
    <Card bodyStyle={styles.body}>
      <ActivityIndicator color={appTheme.colors.primary} size="small" />
      <Text variant="sectionTitle">{title}</Text>
      {description ? (
        <Text variant="body" tone="secondary" style={styles.description}>
          {description}
        </Text>
      ) : null}
    </Card>
  );
}

const styles = StyleSheet.create({
  body: {
    gap: appTheme.spacing.sm,
    alignItems: 'center',
  },
  description: {
    textAlign: 'center',
  },
});
