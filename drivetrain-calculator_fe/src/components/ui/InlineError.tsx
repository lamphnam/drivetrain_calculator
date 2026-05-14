import { StyleSheet, View } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

type InlineErrorProps = {
  message: string;
};

export function InlineError({ message }: InlineErrorProps) {
  return (
    <View style={styles.container}>
      <Text variant="caption" tone="error">
        {message}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    borderRadius: appTheme.radii.sm,
    paddingHorizontal: appTheme.spacing.sm,
    paddingVertical: 10,
    backgroundColor: appTheme.colors.errorSoft,
    borderWidth: 1,
    borderColor: '#F2B8B8',
  },
});
