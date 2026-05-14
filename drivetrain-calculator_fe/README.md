# CMD System

Expo + TypeScript + Expo Router bootstrap for a mechanical design calculator app. This repository currently focuses on production-minded structure, architectural boundaries, and placeholder screens for Module 1.

## Start

```bash
npm install
npx expo start
```

## Build For Real Devices (No App Store / Play Store)

Use EAS internal distribution to generate install links:

1. Install EAS CLI globally:

```bash
npm install -g eas-cli
```

2. Log in and configure once:

```bash
eas login
eas build:configure
```

3. Build Android APK (direct install):

```bash
eas build -p android --profile preview
```

4. Build iOS IPA (internal distribution):

```bash
eas build -p ios --profile preview
```

After each build, EAS gives you a URL/QR code. Share that link so users can install directly.

## Why Expo Go Fails Right Now

This project is currently pinned to `55.x canary` Expo packages. App Store / Play Store Expo Go supports stable SDK versions, not canary builds.

To run in Expo Go:

1. Move dependencies from canary to stable Expo SDK (recommended: latest stable SDK).
2. Run `expo install --fix`.
3. Start with `expo start --clear` and open in Expo Go.

If you keep canary SDK, use a Development Build instead of Expo Go:

```bash
eas build -p android --profile development
eas build -p ios --profile development
```

## Key Docs

- [ARCHITECTURE.md](./ARCHITECTURE.md)
- [TODO_BOOTSTRAP.md](./TODO_BOOTSTRAP.md)

## Current Scope

- File-based routing under `src/app`
- Shared theme and reusable component layer
- Feature boundary for Module 1
- Reserved folders for mocks, services, store, utilities, and tests

## Next

Implement the Module 1 domain model, form flow, calculation engine, persistence approach, and automated tests in that order.
