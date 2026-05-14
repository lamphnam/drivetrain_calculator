import { ActivityIndicator, Pressable, StyleSheet, View, type StyleProp, type ViewStyle } from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

export type ButtonProps = {
  label: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary';
  isLoading?: boolean;
  disabled?: boolean;
  fullWidth?: boolean;
  style?: StyleProp<ViewStyle>;
};

export function Button({
  label,
  onPress,
  variant = 'primary',
  isLoading = false,
  disabled = false,
  fullWidth = true,
  style,
}: ButtonProps) {
  const shouldDisable = disabled || isLoading;
  const isPrimary = variant === 'primary';

  return (
    <Pressable
      accessibilityRole="button"
      disabled={shouldDisable}
      onPress={onPress}
      style={({ pressed }) => [
        styles.base,
        fullWidth ? styles.fullWidth : styles.autoWidth,
        isPrimary ? styles.primary : styles.secondary,
        shouldDisable ? styles.disabled : null,
        pressed && !shouldDisable ? styles.pressed : null,
        style,
      ]}>
      {isLoading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={isPrimary ? appTheme.colors.card : appTheme.colors.primary} />
        </View>
      ) : (
        <Text variant="button" tone={isPrimary ? 'inverse' : 'accent'}>
          {label}
        </Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    minHeight: 50,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: appTheme.radii.md,
    borderWidth: 1,
    paddingHorizontal: appTheme.spacing.md,
  },
  fullWidth: {
    width: '100%',
  },
  autoWidth: {
    alignSelf: 'flex-start',
  },
  primary: {
    backgroundColor: appTheme.colors.primary,
    borderColor: appTheme.colors.primary,
  },
  secondary: {
    backgroundColor: appTheme.colors.card,
    borderColor: appTheme.colors.border,
  },
  disabled: {
    backgroundColor: appTheme.colors.disabled,
    borderColor: appTheme.colors.disabled,
  },
  pressed: {
    opacity: 0.94,
  },
  loadingWrap: {
    minHeight: 20,
    justifyContent: 'center',
  },
});
