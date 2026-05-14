import { colors } from './colors';
import { radii, spacing } from './spacing';
import { fontFamilies, typography } from './typography';

export const appTheme = {
  colors,
  spacing,
  radii,
  typography,
  fontFamilies,
  elevation: {
    card: {
      shadowColor: '#132033',
      shadowOffset: { width: 0, height: 8 },
      shadowOpacity: 0.08,
      shadowRadius: 18,
      elevation: 2,
    },
  },
} as const;
