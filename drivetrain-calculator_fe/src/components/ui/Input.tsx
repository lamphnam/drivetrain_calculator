import {
  StyleSheet,
  TextInput,
  View,
  type StyleProp,
  type TextInputProps,
  type ViewStyle,
} from 'react-native';

import { appTheme } from '@/theme';
import { Text } from './Text';

export type InputProps = TextInputProps & {
  label: string;
  helperText?: string;
  errorText?: string;
  unit?: string;
  style?: StyleProp<ViewStyle>;
};

export function Input({
  label,
  helperText,
  errorText,
  unit,
  style,
  ...inputProps
}: InputProps) {
  return (
    <View style={[styles.wrapper, style]}>
      <Text variant="bodySmallStrong">{label}</Text>

      <View style={[styles.shell, errorText ? styles.shellError : null]}>
        <TextInput
          placeholderTextColor={appTheme.colors.textMuted}
          selectionColor={appTheme.colors.primary}
          style={styles.input}
          {...inputProps}
        />
        {unit ? <Text variant="bodySmallStrong" tone="secondary">{unit}</Text> : null}
      </View>

      {errorText ? (
        <Text variant="caption" tone="error">
          {errorText}
        </Text>
      ) : helperText ? (
        <Text variant="caption" tone="secondary">
          {helperText}
        </Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: appTheme.spacing.xs,
  },
  shell: {
    minHeight: 52,
    flexDirection: 'row',
    alignItems: 'center',
    gap: appTheme.spacing.sm,
    borderRadius: appTheme.radii.md,
    borderWidth: 1,
    borderColor: appTheme.colors.border,
    backgroundColor: appTheme.colors.card,
    paddingHorizontal: appTheme.spacing.md,
  },
  shellError: {
    borderColor: appTheme.colors.error,
  },
  input: {
    flex: 1,
    paddingVertical: appTheme.spacing.sm,
    color: appTheme.colors.textPrimary,
    fontSize: appTheme.typography.body.fontSize,
    lineHeight: appTheme.typography.body.lineHeight,
  },
});
