# Architecture

This Expo repository uses TypeScript with Expo Router and keeps file-based routes under `src/app`. Route files stay thin and delegate rendering to screen components inside feature folders or shared presentation layers.

## Structure

- `src/app`: Expo Router entrypoints only. No business logic or mock data should live here.
- `src/features`: Feature-owned screens, domain types, and later feature services or hooks.
- `src/components`: Shared presentational building blocks that must not import feature logic.
- `src/services`: Shared API client and cross-feature data access contracts.
- `src/mocks`: Reserved for future mock handlers and fixtures.
- `src/theme`: Design tokens and app-wide theme values.
- `src/types`: Shared domain types that can be reused across features.
- `src/constants`: Stable app constants and route literals.
- `src/hooks`: Shared reusable hooks.
- `src/store`: Lightweight client state when needed.
- `src/utils`: Pure helper functions with no framework dependency.
- `tests`: Future automated test suites.

## Boundaries

- Route files in `src/app` should import screens, not implement domain logic.
- Shared UI components should remain feature-agnostic.
- Module 1 engineering rules should live inside `src/features/module1`.
- Network details should enter through `src/services/api`.
- Mock infrastructure should stay isolated from route files and production services.

## Navigation Choice

Tabs are intentionally not introduced on day one. The information architecture is still fluid, and a simple stack keeps the bootstrap easier to maintain until the primary workflows are confirmed.
