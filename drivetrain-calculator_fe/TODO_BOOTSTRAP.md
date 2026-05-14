# Bootstrap TODO

1. Confirm product naming, app icon direction, and the final route map for the first release.
2. Define Module 1 input schema, domain terminology, and result model in `src/features/module1`.
3. Build the Module 1 input form screen with validation and navigation to the result screen.
4. Implement the Module 1 calculation engine and keep it separate from presentation components.
5. Introduce lightweight local state for in-progress drafts and saved calculations.
6. Add a persistence strategy for saved calculations before wiring the history screen.
7. Decide whether the app needs a mock layer first or a real API client first, then implement `src/mocks` or `src/services/api`.
8. Add automated tests for pure calculation logic before expanding UI complexity.
9. Revisit navigation and add tabs only if the finalized information architecture benefits from them.
