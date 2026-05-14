# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Full-stack mechanical engineering calculator for a mixing drum drivetrain system. Models the power transmission chain: Electric Motor → V-belt → Bevel Gear → Spur Gear → Drum Shaft.

Two sub-projects:
- `drivetrain-calculator_be/` — Java 17 Spring Boot REST API (Maven)
- `drivetrain-calculator_fe/` — React Native / Expo mobile app (TypeScript)

## Backend commands

Run from `drivetrain-calculator_be/`:

```bash
./mvnw spring-boot:run          # start the app (requires Neon PostgreSQL)
./mvnw test                     # run all tests
./mvnw -Dtest=Module4EngineeringCalculatorTests test  # single test class
./mvnw package                  # build jar
```

Swagger UI at `/swagger-ui.html` when running. No lint/static-analysis configured.

## Frontend commands

Run from `drivetrain-calculator_fe/`:

```bash
npm install
npx expo start                  # start dev server
```

Requires a development build or EAS build (Expo SDK 55 canary — standard Expo Go won't work). Set `EXPO_PUBLIC_API_BASE_URL` to point at the backend (defaults to `http://localhost:8080`).

## Architecture

### Calculation flow (dependency order)

1. **Module 1** — motor selection + transmission ratio distribution. Takes required power (kW) and output RPM, selects motor from catalog, computes ratios (belt U1, bevel U2, spur U3), propagates shaft states through 5 shafts.
2. **Module 3** — straight bevel gear design. Takes Module 1 SHAFT_1 state + gear material, selects standard module (2–12mm), verifies contact/bending stress.
3. **Module 4** — straight spur gear design. Takes Module 1 SHAFT_2 state + U3, selects standard module (1–12mm), verifies stress.
4. **Full Flow** — orchestrates Module 1 → 3 → 4 in one request.

### Invalidation cascade

Recalculating Module 1 deletes Module 3 and Module 4 results. Recalculating Module 3 deletes Module 4 results.

### Backend structure (`com.drivetrain`)

Module-per-feature layout: `module1/`, `module3/`, `module4/`, `fullflow/` — each with `controller/`, `dto/`, `service/`, `exception/` sub-packages. Shared domain under `domain/entity/`, `domain/enums/`, `domain/repository/`.

Key conventions:
- `BigDecimal` with `MathContext(16, HALF_UP)` and scale=6 for all engineering math
- Pure engineering calculators (`Module3EngineeringCalculator`, `Module4EngineeringCalculator`) are package-private, non-Spring, stateless — unit-testable in isolation
- Full-flow service reuses module services at the Java method level (no internal HTTP calls)
- `DataInitializer` seeds reference data on startup (idempotent)
- Database: Neon PostgreSQL (cloud-only, no local fallback), `ddl-auto: update`

Known placeholder: `bevelGearRatioU2 = 3.14` is hardcoded in `Module1CalculationService` — the U2 feedback loop from Module 3 is not yet implemented.

### Frontend structure (`src/`)

- `app/` — Expo Router route files only (thin, delegate to screens)
- `features/module1|module3|module4/` — screens, hooks, components, types, state per feature
- `services/api/` — fetch-based HTTP client + typed API functions + mock handlers
- `components/ui/` — shared presentational components (feature-agnostic)
- `constants/uiText.ts` — all UI strings centralized here
- `store/` — in-memory session history via `useSyncExternalStore` (not persisted)
- `theme/` — design tokens (colors, spacing, typography)

Navigation flow: Home → Module 1 → Module 3 → Module 4 (sequential, each result screen has a "proceed" button to the next module).

### REST endpoints

- Module 1: `POST /api/v1/module-1/calculate`, `GET /reference-values`, `GET /history`, `GET /history/{id}`
- Module 3: `POST /api/v1/module-3/calculate`, `GET /materials`, `GET /history/{id}`
- Module 4: `POST /api/v1/module-4/calculate`, `GET /history/{id}`
- Full Flow: `POST /api/v1/drivetrain/full-flow/calculate`, `GET /history/{id}`

## Testing

Backend tests are minimal — context load smoke test + unit tests for the two engineering calculators. Frontend has no automated tests yet. Mock handlers in `src/services/api/mocks/` simulate all three modules with 280ms delay.

## Postman

`drivetrain-calculator_be/postman/` contains a collection and local environment file for all endpoints.
