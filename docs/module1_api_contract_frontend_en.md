# Module 1 API Contract for Frontend

## Purpose

Module 1 is the direct calculation entry point for the mechanical design calculator.

The frontend does not need to create a `DesignCase` before calling the calculation API.
The backend accepts the user inputs directly, performs the Module 1 calculation, persists the saved record, and returns a result payload ready for the result screen.

Base path:

```text
/api/v1/module-1
```

## Endpoints

### 1. Calculate Module 1 result

- Method: `POST`
- Path: `/api/v1/module-1/calculate`
- Content-Type: `application/json`

#### Request body

```json
{
  "requiredPowerKw": 1.2,
  "requiredOutputRpm": 40,
  "constantSetId": 1,
  "caseCode": "CASE-001",
  "caseName": "Module 1 Test"
}
```

#### Request field definitions

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `requiredPowerKw` | `number` | Yes | Required output power in kW. Must be greater than `0`. |
| `requiredOutputRpm` | `number` | Yes | Required output speed in rpm. Must be greater than `0`. |
| `constantSetId` | `number` | No | If provided, the backend loads that exact constant set. If omitted, the backend uses the default active constant set. |
| `caseCode` | `string` | No | Optional business key for the saved calculation. If an existing case with the same code is found, the backend recalculates and replaces the previous Module 1 result for that case. |
| `caseName` | `string` | No | Optional display name for the saved calculation. |

#### Successful response

- Status: `200 OK`

```json
{
  "resultInfo": {
    "resultId": 12,
    "createdAt": "2026-04-11T11:24:15.230Z",
    "updatedAt": "2026-04-11T11:24:15.230Z"
  },
  "caseInfo": {
    "designCaseId": 8,
    "caseCode": "CASE-001",
    "caseName": "Module 1 Test",
    "status": "MODULE1_COMPLETED"
  },
  "inputSummary": {
    "requiredPowerKw": 1.200000,
    "requiredOutputRpm": 40.000000
  },
  "referenceSummary": {
    "constantSetId": 1,
    "constantSetCode": "DEFAULT_SET_V1",
    "constantSetName": "Default Module 1 Constants",
    "availableMotorsCount": 9,
    "defaultBeltRatioU1": 3.600000,
    "defaultGearboxRatioUh": 11.500000
  },
  "selectedMotor": {
    "motorId": 1,
    "motorCode": "4A80B2Y6",
    "displayName": "2.2 kW, 2880 rpm",
    "manufacturer": "Standard",
    "description": "2.2 kW, 2880 rpm",
    "ratedPowerKw": 2.200000,
    "ratedRpm": 2880.000000
  },
  "systemEfficiency": 0.876365,
  "requiredMotorPowerKw": 1.369292,
  "preliminaryMotorRpmNsb": 1656.000000,
  "transmissionRatios": {
    "overallRatio": 72.000000,
    "beltRatioU1": 3.600000,
    "gearboxRatioUh": 20.000000,
    "bevelRatioU2": 3.140000,
    "spurRatioU3": 6.369427
  },
  "shaftStates": [
    {
      "shaftCode": "MOTOR",
      "shaftLabel": "Motor Shaft",
      "sequenceNo": 1,
      "powerKw": 2.200000,
      "rpm": 2880.000000,
      "torqueNmm": 7295.138889
    },
    {
      "shaftCode": "SHAFT_1",
      "shaftLabel": "Shaft 1",
      "sequenceNo": 2,
      "powerKw": 2.091650,
      "rpm": 800.000000,
      "torqueNmm": 24967.821875
    },
    {
      "shaftCode": "SHAFT_2",
      "shaftLabel": "Shaft 2",
      "sequenceNo": 3,
      "powerKw": 1.998672,
      "rpm": 254.777070,
      "torqueNmm": 74930.849791
    },
    {
      "shaftCode": "SHAFT_3",
      "shaftLabel": "Shaft 3",
      "sequenceNo": 4,
      "powerKw": 1.928649,
      "rpm": 40.000000,
      "torqueNmm": 460014.873750
    },
    {
      "shaftCode": "DRUM_SHAFT",
      "shaftLabel": "Output Drum Shaft",
      "sequenceNo": 5,
      "powerKw": 1.928649,
      "rpm": 40.000000,
      "torqueNmm": 460014.873750
    }
  ],
  "calculationNotes": [
    "Constant set DEFAULT_SET_V1 was used for this Module 1 calculation.",
    "Motor selection only considers active motors with rated power greater than or equal to the required motor power, then picks the rpm closest to the preliminary motor rpm.",
    "Shaft power propagation applies etaOl across three transitions so the shaft states remain consistent with etaKn * etaD * etaBrc * etaBrt * etaOl^3.",
    "Bevel gear ratio U2 = 3.14 is a temporary placeholder until Module 3 provides the real gearbox split."
  ]
}
```

#### Response field definitions

##### `resultInfo`

| Field | Type | Description |
| --- | --- | --- |
| `resultId` | `number` | Primary key of the persisted Module 1 result. |
| `createdAt` | `string` | ISO-8601 timestamp when the result row was created. |
| `updatedAt` | `string` | ISO-8601 timestamp when the result row was last updated. |

##### `caseInfo`

| Field | Type | Description |
| --- | --- | --- |
| `designCaseId` | `number` | Primary key of the saved calculation root. |
| `caseCode` | `string` | Saved case code. |
| `caseName` | `string` | Saved case display name. |
| `status` | `string` | Current case status. Current successful value: `MODULE1_COMPLETED`. |

##### `inputSummary`

| Field | Type | Description |
| --- | --- | --- |
| `requiredPowerKw` | `number` | Requested output power in kW. |
| `requiredOutputRpm` | `number` | Requested output speed in rpm. |

##### `referenceSummary`

| Field | Type | Description |
| --- | --- | --- |
| `constantSetId` | `number` | Constant set ID used for the calculation. |
| `constantSetCode` | `string` | Constant set code. |
| `constantSetName` | `string` | Constant set name. |
| `availableMotorsCount` | `number` | Number of active motors currently available for selection. |
| `defaultBeltRatioU1` | `number` | Default belt ratio from the selected constant set. |
| `defaultGearboxRatioUh` | `number` | Default gearbox ratio from the selected constant set. |

##### `selectedMotor`

| Field | Type | Description |
| --- | --- | --- |
| `motorId` | `number` | Motor database ID. |
| `motorCode` | `string` | Motor catalog code. |
| `displayName` | `string` | Frontend-friendly motor label. Usually uses description when available. |
| `manufacturer` | `string \| null` | Manufacturer if available. |
| `description` | `string \| null` | Catalog description if available. |
| `ratedPowerKw` | `number` | Rated motor power in kW. |
| `ratedRpm` | `number` | Rated motor speed in rpm. |

##### `transmissionRatios`

| Field | Type | Description |
| --- | --- | --- |
| `overallRatio` | `number` | Actual overall transmission ratio. |
| `beltRatioU1` | `number` | Belt ratio `U1`. |
| `gearboxRatioUh` | `number` | Gearbox transmission ratio `Uh`. |
| `bevelRatioU2` | `number` | Temporary bevel gear ratio `U2`. |
| `spurRatioU3` | `number` | Spur gear ratio `U3`. |

##### `shaftStates[]`

| Field | Type | Description |
| --- | --- | --- |
| `shaftCode` | `string` | One of `MOTOR`, `SHAFT_1`, `SHAFT_2`, `SHAFT_3`, `DRUM_SHAFT`. |
| `shaftLabel` | `string` | Frontend-ready display label. |
| `sequenceNo` | `number` | Stable order for rendering. |
| `powerKw` | `number` | Shaft power in kW. |
| `rpm` | `number` | Shaft speed in rpm. |
| `torqueNmm` | `number` | Shaft torque in N.mm. |

##### `calculationNotes[]`

An ordered list of backend notes, assumptions, and temporary warnings that can be displayed in the result screen.

## History APIs

### 2. List saved Module 1 calculations

- Method: `GET`
- Path: `/api/v1/module-1/history`

#### Successful response

- Status: `200 OK`

```json
[
  {
    "designCaseId": 8,
    "resultId": 12,
    "moduleLabel": "Module 1",
    "caseCode": "CASE-001",
    "caseName": "Module 1 Test",
    "requiredPowerKw": 1.200000,
    "requiredOutputRpm": 40.000000,
    "selectedMotorCode": "4A80B2Y6",
    "selectedMotorDisplayName": "2.2 kW, 2880 rpm",
    "savedAt": "2026-04-11T11:24:15.230Z",
    "updatedAt": "2026-04-11T11:24:15.230Z"
  }
]
```

#### History item fields

| Field | Type | Description |
| --- | --- | --- |
| `designCaseId` | `number` | ID used to reopen the saved calculation. |
| `resultId` | `number` | Module 1 result ID. |
| `moduleLabel` | `string` | Fixed label for UI display. |
| `caseCode` | `string` | Saved case code. |
| `caseName` | `string` | Saved case name. |
| `requiredPowerKw` | `number` | Saved input power. |
| `requiredOutputRpm` | `number` | Saved input speed. |
| `selectedMotorCode` | `string` | Selected motor code. |
| `selectedMotorDisplayName` | `string` | Selected motor label for list cards. |
| `savedAt` | `string` | Result creation timestamp. |
| `updatedAt` | `string` | Last update timestamp. |

### 3. Reopen one saved Module 1 calculation

- Method: `GET`
- Path: `/api/v1/module-1/history/{designCaseId}`

#### Path parameter

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `designCaseId` | `number` | Yes | Saved design case ID from the history list. Must be greater than `0`. |

#### Successful response

- Status: `200 OK`
- Response body: same shape as `POST /calculate`

## Validation and error responses

### Common status codes

| Status | When |
| --- | --- |
| `200` | Request completed successfully |
| `404` | Saved design case not found for history detail |
| `422` | Validation error, missing constant set, invalid numeric input, or no suitable motor found |

### Example validation error

```json
{
  "timestamp": "2026-04-11T11:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/v1/module-1/calculate"
}
```

Note:

- Exact validation error shape depends on the global Spring Boot exception handling configuration.
- Frontend should treat `4xx` responses as user-fixable input or data issues.

## Frontend integration notes

- Call `POST /calculate` directly from the New Calculation screen.
- Do not create a separate case before calculation.
- Use `caseInfo.designCaseId` as the stable key for reopening saved results.
- Use `shaftStates` in ascending `sequenceNo` order.
- Show `calculationNotes` in the result screen when available.
- `bevelRatioU2` is currently a placeholder value for Module 1 only and may change when later modules are implemented.

