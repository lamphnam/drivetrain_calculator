/**
 * Root Expo Router layout that owns the app-wide stack and screen chrome.
 */

import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';

import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';

export default function RootLayout() {
  return (
    <>
      <StatusBar style="dark" />
      <Stack
        screenOptions={{
          headerStyle: {
            backgroundColor: appTheme.colors.card,
          },
          headerTintColor: appTheme.colors.textPrimary,
          headerTitleStyle: {
            fontSize: appTheme.typography.sectionTitle.fontSize,
            fontWeight: appTheme.typography.sectionTitle.fontWeight,
          },
          contentStyle: {
            backgroundColor: appTheme.colors.background,
          },
        }}>
        <Stack.Screen name="index" options={{ title: UI_TEXT.nav.home }} />
        <Stack.Screen
          name="calculations/new"
          options={{ title: UI_TEXT.nav.newCalculation }}
        />
        <Stack.Screen
          name="calculations/result"
          options={{ title: UI_TEXT.nav.calculationResults }}
        />
        <Stack.Screen
          name="calculations/history"
          options={{ title: UI_TEXT.nav.savedCalculations }}
        />
        <Stack.Screen name="settings" options={{ title: UI_TEXT.nav.settings }} />
      </Stack>
    </>
  );
}
