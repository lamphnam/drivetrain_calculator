/**
 * Central route literals for use outside route files when screens need to link to other flows.
 */

export const routes = {
  home: '/' as const,
  calculationsNew: '/calculations/new' as const,
  calculationsResult: '/calculations/result' as const,
  calculationsHistory: '/calculations/history' as const,
  module3New: '/module-3/new' as const,
  module3Result: '/module-3/result' as const,
  module4New: '/module-4/new' as const,
  module4Result: '/module-4/result' as const,
  settings: '/settings' as const,
};
