# Backend Integration Guide: Module 1, 3 & 4

This guide is for backend engineers who need to replace the current frontend mocks with real HTTP calls to the Spring Boot API.

## 1. Key Integration Files

| Component | File Path | Purpose |
| --- | --- | --- |
| **API Client** | [apiClient.ts](/cmd-system/src/services/api/apiClient.ts) | The central HTTP client (Axios/Fetch). Implement this first. |
| **Module 1 Service** | [module1Api.ts](/cmd-system/src/services/api/module1Api.ts) | Service layer for Motor selection and Transmission design. |
| **Module 3 Service** | [module3Api.ts](/cmd-system/src/services/api/module3Api.ts) | Service layer for Straight Bevel Gear stage. |
| **Module 4 Service** | [module4Api.ts](/cmd-system/src/services/api/module4Api.ts) | Service layer for Straight Spur Gear stage. |
| **Contracts** | `src/services/api/contracts/*.contract.ts` | Centralized endpoint paths and method definitions. |
| **Data Types** | `src/types/api/*.ts` | Request/Response DTOs matching the backend contract. |

---

## 2. Integration Steps

### Step A: Implement the `apiClient`
Currently, `src/services/api/apiClient.ts` only throws errors. You should implement it using `axios` or `fetch`. Ensure you handle base URLs (likely from `.env`) and common headers (e.g., `Content-Type: application/json`).

### Step B: Swap Mocks in API Services
Go to `src/services/api/module1Api.ts`, `src/services/api/module3Api.ts`, and `src/services/api/module4Api.ts` and replace the mock calls with `apiClient` calls.

**Example for Module 1 Calculation:**
```typescript
// src/services/api/module1Api.ts

export const module1Api: Module1ApiContract = {
  // ...
  async createCalculation(request) {
    // OLD: return createModule1CalculationMock(request);
    return apiClient.post(module1ApiEndpoints.createCalculation, request);
  },
  // ...
};
```

### Step C: Verify Error Formats
The frontend expects errors in the following format (defined in `src/types/api/common.ts`):

```json
{
  "timestamp": "2026-05-14T10:00:00Z",
  "status": 400,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input",
    "fieldErrors": [
      { "field": "materialId", "reason": "Material not found" }
    ]
  },
  "path": "/api/v1/module-3/calculate"
}
```
If your Spring Boot `ControllerAdvice` produces a different shape, update the `extractApiErrorResponse` helper in the hooks or modify the backend to match this standard.

---

## 3. Handling Loading and Error States

The frontend is already designed to handle loading and error states automatically through custom hooks.

- **Loading**: Every screen using `useModule3Calculation` or `useModule3History` tracks `isSubmitting` or `isLoading`. It will automatically show a `<LoadingState />` overlay.
- **Errors**: If the API returns a 4xx or 5xx status, the hook catches it and populates `submissionError` or `bootstrapError`. The screen then renders an `<InlineError />` component.

**Testing Network Issues:**
To test how the UI reacts to a slow or failing network:
1. Increase the delay in `src/services/api/mocks/handlers/module3.handler.ts`.
2. Force an error by throwing an exception in the mock handler.

---

## 4. Testing the Full Flow

1. **Local Dev**: Run `npm start` to start the Expo dev server.
2. **Module 1**: Perform a Module 1 calculation to get a `designCaseId`.
3. **Module 3**: Click **Proceed to Bevel Gear Design** from the result screen.
4. **Network Log**: Use React Native Debugger or Chrome DevTools (if running in web mode) to inspect the network requests made to `/api/v1/module-3/*`.

---

## 5. API Endpoints Reference

### Module 1
- `GET /catalog/motors`
- `GET /system/constants/module-1`
- `POST /calculations/module-1`

### Module 3
- `GET /api/v1/module-3/materials`
- `POST /api/v1/module-3/calculate`
- `GET /api/v1/module-3/history/{designCaseId}`

### Module 4
- `POST /api/v1/module-4/calculate`
- `GET /api/v1/module-4/history/{designCaseId}`
