# Module 3 API Contract, Entity Guide, and Test Cases

## Purpose

This document describes the **current implemented backend contract** for **Module 3** in this Spring Boot project.

Module 3 is the **straight bevel gear stage** module.
It:
- consumes a saved `DesignCase`
- depends on an existing **Module 1 result**
- selects a gear material
- calculates bevel gear geometry
- calculates allowable stresses
- checks contact and bending stress pass/fail
- outputs force vectors for shaft 1 and shaft 2
- persists the result for later reopening

This document covers:
1. REST API contract
2. request and response shape
3. error behavior
4. persistence entities and attributes
5. test scenarios to validate Module 3
6. current assumptions and placeholders

---

## Base Path

```text
/api/v1/module-3
```

---

## High-Level Workflow

Module 3 is **not standalone** in the current implementation.
It expects a saved `DesignCase` that already has a valid **Module 1** result.

Typical frontend or tester flow:

1. Call `POST /api/v1/module-1/calculate`
2. Get `caseInfo.designCaseId`
3. Call `GET /api/v1/module-3/materials`
4. Pick `materialId`
5. Call `POST /api/v1/module-3/calculate`
6. Optionally reopen with `GET /api/v1/module-3/history/{designCaseId}`

---

# 1. API Endpoints

## 1.1 Get available gear materials

- **Method:** `GET`
- **Path:** `/api/v1/module-3/materials`

### Successful response
- **Status:** `200 OK`

### Example response

```json
[
  {
    "materialId": 1,
    "materialCode": "C40XH_QT",
    "materialName": "Steel C40XH",
    "heatTreatment": "Quenched and tempered",
    "hbMin": 235.000000,
    "hbMax": 262.000000,
    "sigmaBMpa": 850.000000,
    "sigmaChMpa": 650.000000
  },
  {
    "materialId": 2,
    "materialCode": "40CR_N",
    "materialName": "Steel 40Cr",
    "heatTreatment": "Normalized",
    "hbMin": 207.000000,
    "hbMax": 241.000000,
    "sigmaBMpa": 780.000000,
    "sigmaChMpa": 540.000000
  },
  {
    "materialId": 3,
    "materialCode": "C45_N",
    "materialName": "Steel C45",
    "heatTreatment": "Normalized",
    "hbMin": 179.000000,
    "hbMax": 207.000000,
    "sigmaBMpa": 600.000000,
    "sigmaChMpa": 355.000000
  }
]
```

### Response fields

| Field | Type | Description |
| --- | --- | --- |
| `materialId` | `number` | Primary key of the seeded/saved material |
| `materialCode` | `string` | Stable material code |
| `materialName` | `string` | Display name |
| `heatTreatment` | `string \| null` | Heat-treatment description |
| `hbMin` | `number` | Minimum Brinell hardness |
| `hbMax` | `number` | Maximum Brinell hardness |
| `sigmaBMpa` | `number` | Material tensile strength input used by Module 3 |
| `sigmaChMpa` | `number` | Material yield-strength input used by Module 3 |

---

## 1.2 Calculate Module 3 result

- **Method:** `POST`
- **Path:** `/api/v1/module-3/calculate`
- **Content-Type:** `application/json`

### Request body

```json
{
  "designCaseId": 8,
  "inputT1Nmm": 83851.991000,
  "inputN1Rpm": 812.000000,
  "inputU2": 3.140000,
  "serviceLifeHours": 43200.000000,
  "materialId": 1
}
```

### Request field definitions

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `designCaseId` | `number` | Yes | Saved design case ID. Must already exist. |
| `inputT1Nmm` | `number` | No | Input torque on shaft 1 in N.mm. If omitted, backend uses `Module 1 -> SHAFT_1.torqueNmm`. |
| `inputN1Rpm` | `number` | No | Input rpm on shaft 1. If omitted, backend uses `Module 1 -> SHAFT_1.rpm`. |
| `inputU2` | `number` | No | Requested bevel ratio. If omitted, backend uses `Module 1 -> bevelGearRatioU2`. |
| `serviceLifeHours` | `number` | No | Required service life `Lh`. If omitted, backend uses saved `DesignCase.serviceLifeHours`; if still empty, defaults to `43200`. |
| `materialId` | `number` | Yes | Chosen material ID from `/materials`. |

### Input resolution rules

The current implementation resolves Module 3 input values in this order:

#### Torque `inputT1Nmm`
1. Request body value if provided
2. Else `Module 1` shaft state with `shaftCode = SHAFT_1`

#### Speed `inputN1Rpm`
1. Request body value if provided
2. Else `Module 1` shaft state with `shaftCode = SHAFT_1`

#### Ratio `inputU2`
1. Request body value if provided
2. Else saved `Module 1` field `bevelGearRatioU2`

#### Service life `serviceLifeHours`
1. Request body value if provided
2. Else saved `DesignCase.serviceLifeHours`
3. Else backend default `43200`

---

### Successful response

- **Status:** `200 OK`

### Example response

> Notes:
> - IDs and timestamps depend on your database.
> - The numeric values below match the current implementation for the reference-like input:
>   `T1 = 83851.991`, `n1 = 812`, `U2 = 3.14`, `Lh = 43200`, material `C40XH_QT`.

```json
{
  "resultInfo": {
    "resultId": 5,
    "createdAt": "2026-04-19T10:30:45.123Z",
    "updatedAt": "2026-04-19T10:30:45.123Z"
  },
  "caseInfo": {
    "designCaseId": 8,
    "caseCode": "CASE-001",
    "caseName": "Module 1 Test",
    "status": "MODULE3_COMPLETED"
  },
  "inputSummary": {
    "inputT1Nmm": 83851.991000,
    "inputN1Rpm": 812.000000,
    "inputU2": 3.140000,
    "serviceLifeHours": 43200.000000
  },
  "selectedMaterial": {
    "materialId": 1,
    "materialCode": "C40XH_QT",
    "materialName": "Steel C40XH",
    "heatTreatment": "Quenched and tempered",
    "hbMin": 235.000000,
    "hbMax": 262.000000,
    "sigmaBMpa": 850.000000,
    "sigmaChMpa": 650.000000
  },
  "allowableStresses": {
    "allowableContactStressMpa": 608.940000,
    "allowableBendingStressMpa": 258.400000
  },
  "gearGeometry": {
    "reCalculated": 133.912986,
    "de1Calculated": 81.000000,
    "moduleMteSelected": 3.000000,
    "teethZ1": 27,
    "teethZ2": 85,
    "actualRatioU2": 3.148148,
    "widthBMm": 38.000000,
    "diameterDm1Mm": 69.495849,
    "diameterDm2Mm": 218.783229,
    "coneAngleDelta1Deg": 17.622297,
    "coneAngleDelta2Deg": 72.377703
  },
  "stressCheck": {
    "sigmaHMpa": 556.244921,
    "sigmaF1Mpa": 92.382408,
    "sigmaF2Mpa": 88.730929,
    "contactStressPass": true,
    "bendingStressPass": true
  },
  "shaftForces": [
    {
      "shaftCode": "SHAFT_1",
      "shaftLabel": "Shaft 1",
      "ftN": 2413.151065,
      "frN": 837.098398,
      "faN": 265.901841
    },
    {
      "shaftCode": "SHAFT_2",
      "shaftLabel": "Shaft 2",
      "ftN": 2413.151065,
      "frN": 265.901841,
      "faN": 837.098398
    }
  ],
  "calculationNotes": [
    "Input torque T1 was inherited from Module 1 shaft state SHAFT_1.",
    "Input speed n1 was inherited from Module 1 shaft state SHAFT_1.",
    "Input bevel ratio U2 was inherited from stored Module 1 ratio U2.",
    "Service life hours Lh defaulted to 43200 h because no explicit value was saved yet.",
    "Material C40XH_QT was used to derive allowable contact and bending stresses.",
    "Allowable stress evaluation currently uses a seeded material-strength model with simplified life-factor and tooth-form approximations pending finalized handbook/table lookups.",
    "When U2 is inherited from Module 1 it may still reflect the temporary placeholder split until a finalized upstream gearbox ratio allocation is available.",
    "Selected module and tooth counts keep the actual bevel ratio within the 4% target of the requested ratio.",
    "Actual ratio error versus requested U2: 0.259495%."
  ]
}
```

---

### Response field definitions

#### `resultInfo`

| Field | Type | Description |
| --- | --- | --- |
| `resultId` | `number` | Primary key of persisted `Module3Result` |
| `createdAt` | `string` | ISO-8601 creation timestamp |
| `updatedAt` | `string` | ISO-8601 last update timestamp |

#### `caseInfo`

| Field | Type | Description |
| --- | --- | --- |
| `designCaseId` | `number` | Design case root ID |
| `caseCode` | `string` | Saved case code |
| `caseName` | `string` | Saved case name |
| `status` | `string` | Current case status. Current successful status: `MODULE3_COMPLETED` |

#### `inputSummary`

| Field | Type | Description |
| --- | --- | --- |
| `inputT1Nmm` | `number` | Actual input torque used by the calculation |
| `inputN1Rpm` | `number` | Actual input speed used |
| `inputU2` | `number` | Requested or inherited bevel ratio used |
| `serviceLifeHours` | `number` | Service life used in the calculation |

#### `selectedMaterial`

| Field | Type | Description |
| --- | --- | --- |
| `materialId` | `number` | Material primary key |
| `materialCode` | `string` | Material business key |
| `materialName` | `string` | Display name |
| `heatTreatment` | `string \| null` | Heat-treatment note |
| `hbMin` | `number` | Lower hardness bound |
| `hbMax` | `number` | Upper hardness bound |
| `sigmaBMpa` | `number` | Tensile-strength input used by the backend |
| `sigmaChMpa` | `number` | Yield-strength input used by the backend |

#### `allowableStresses`

| Field | Type | Description |
| --- | --- | --- |
| `allowableContactStressMpa` | `number` | Allowable contact stress used in validation |
| `allowableBendingStressMpa` | `number` | Allowable bending stress used in validation |

#### `gearGeometry`

| Field | Type | Description |
| --- | --- | --- |
| `reCalculated` | `number` | Calculated outer cone distance `Re` |
| `de1Calculated` | `number` | Calculated outer pitch diameter for gear 1 |
| `moduleMteSelected` | `number` | Selected standard outer module |
| `teethZ1` | `number` | Pinion tooth count |
| `teethZ2` | `number` | Gear tooth count |
| `actualRatioU2` | `number` | Ratio actually achieved from `z2 / z1` |
| `widthBMm` | `number` | Face width `b` |
| `diameterDm1Mm` | `number` | Mean diameter of gear 1 |
| `diameterDm2Mm` | `number` | Mean diameter of gear 2 |
| `coneAngleDelta1Deg` | `number` | Cone angle of gear 1 |
| `coneAngleDelta2Deg` | `number` | Cone angle of gear 2 |

#### `stressCheck`

| Field | Type | Description |
| --- | --- | --- |
| `sigmaHMpa` | `number` | Calculated contact stress |
| `sigmaF1Mpa` | `number` | Calculated bending stress on gear 1 |
| `sigmaF2Mpa` | `number` | Calculated bending stress on gear 2 |
| `contactStressPass` | `boolean` | `true` if `sigmaH <= allowableContactStress` |
| `bendingStressPass` | `boolean` | `true` if both gear bending checks pass |

#### `shaftForces[]`

| Field | Type | Description |
| --- | --- | --- |
| `shaftCode` | `string` | Current implementation uses `SHAFT_1` and `SHAFT_2` |
| `shaftLabel` | `string` | Frontend-ready label |
| `ftN` | `number` | Tangential force |
| `frN` | `number` | Radial force |
| `faN` | `number` | Axial force |

#### `calculationNotes[]`

Backend-generated notes that explain:
- where inputs came from
- whether defaults were used
- whether simplified formulas or placeholders are still active
- ratio-error notes and validation-related messages

---

## 1.3 Reopen one saved Module 3 result

- **Method:** `GET`
- **Path:** `/api/v1/module-3/history/{designCaseId}`

### Path parameter

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `designCaseId` | `number` | Yes | Existing saved design case ID |

### Successful response
- **Status:** `200 OK`
- **Response body:** same structure as `POST /api/v1/module-3/calculate`

---

# 2. Error Behavior

## Common status codes

| Status | When |
| --- | --- |
| `200` | Request completed successfully |
| `400` | Bean validation failure from request binding may be returned by Spring for malformed/missing required inputs |
| `404` | `designCaseId` not found, `materialId` not found, or saved Module 3 result not found |
| `422` | Domain-level invalid input or Module 1 prerequisite missing |

## Current exception mapping

| Exception | Status | Meaning |
| --- | --- | --- |
| `Module3DesignCaseNotFoundException` | `404` | Design case ID does not exist |
| `GearMaterialNotFoundException` | `404` | Material ID does not exist |
| `Module3ResultNotFoundException` | `404` | No saved Module 3 result for that case |
| `Module3PrerequisiteMissingException` | `422` | Module 1 result or needed upstream values are missing |
| `InvalidModule3InputException` | `422` | Non-positive or invalid engineering inputs at service level |

### Example 404 response

```json
{
  "timestamp": "2026-04-19T10:40:00.000Z",
  "status": 404,
  "error": "Not Found",
  "path": "/api/v1/module-3/history/999999"
}
```

### Example 422 response

```json
{
  "timestamp": "2026-04-19T10:40:00.000Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "path": "/api/v1/module-3/calculate"
}
```

---

# 3. Persistence Model and Entity Explanation

## Relationship overview

```text
DesignCase    1 --- 1 Module1Result
DesignCase    1 --- 1 Module3Result
GearMaterial  1 --- N Module3Result
Module3Result 1 --- N Module3ShaftForce
```

### What this means in product terms

- `DesignCase` is still the root saved calculation record
- `Module3Result` stores the main bevel-gear design output for that case
- `GearMaterial` is a shared lookup table
- `Module3ShaftForce` stores the force vectors that later shaft design modules can reuse

---

## 3.1 `GearMaterial`

### Role
Stores reusable material reference data for bevel gear calculations.

### Current table name
```text
gear_material
```

### Attributes

| Field | Type | Meaning |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `materialCode` | `String` | Stable business code, e.g. `C40XH_QT` |
| `materialName` | `String` | User-facing material name |
| `heatTreatment` | `String` | Heat-treatment or processing description |
| `hbMin` | `BigDecimal` | Minimum hardness |
| `hbMax` | `BigDecimal` | Maximum hardness |
| `sigmaBMpa` | `BigDecimal` | Material strength input used for stress estimation |
| `sigmaChMpa` | `BigDecimal` | Material yield-strength input used for stress estimation |
| `createdAt` | `Instant` | Creation timestamp |
| `updatedAt` | `Instant` | Update timestamp |
| `module3Results` | `List<Module3Result>` | Back-reference to results using this material |

### Why this entity exists
Without `GearMaterial`, the frontend would have to send raw material strength data every time.
Keeping it in a table makes the workflow cleaner and traceable.

---

## 3.2 `Module3Result`

### Role
Stores the main persisted result of Module 3 for one `DesignCase`.

### Current table name
```text
module3_result
```

### Attributes

| Field | Type | Meaning |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `inputT1Nmm` | `BigDecimal` | Actual torque used by Module 3 |
| `inputN1Rpm` | `BigDecimal` | Actual speed used |
| `inputU2` | `BigDecimal` | Requested/inherited bevel ratio used |
| `serviceLifeHours` | `BigDecimal` | Actual service life used |
| `allowableContactStressMpa` | `BigDecimal` | Allowable contact stress |
| `allowableBendingStressMpa` | `BigDecimal` | Allowable bending stress |
| `reCalculated` | `BigDecimal` | Calculated outer cone distance |
| `de1Calculated` | `BigDecimal` | Calculated outer pitch diameter of gear 1 |
| `moduleMteSelected` | `BigDecimal` | Selected standard outer module |
| `teethZ1` | `Integer` | Pinion tooth count |
| `teethZ2` | `Integer` | Gear tooth count |
| `actualRatioU2` | `BigDecimal` | Actual ratio after integer tooth selection |
| `widthBMm` | `BigDecimal` | Face width |
| `diameterDm1Mm` | `BigDecimal` | Mean diameter of gear 1 |
| `diameterDm2Mm` | `BigDecimal` | Mean diameter of gear 2 |
| `coneAngleDelta1Deg` | `BigDecimal` | Cone angle for gear 1 |
| `coneAngleDelta2Deg` | `BigDecimal` | Cone angle for gear 2 |
| `sigmaHMpa` | `BigDecimal` | Calculated contact stress |
| `sigmaF1Mpa` | `BigDecimal` | Calculated bending stress for gear 1 |
| `sigmaF2Mpa` | `BigDecimal` | Calculated bending stress for gear 2 |
| `contactStressPass` | `boolean` | Whether contact stress passes |
| `bendingStressPass` | `boolean` | Whether bending stress passes |
| `calculationNote` | `String` | Serialized notes/warnings/assumptions |
| `createdAt` | `Instant` | Creation timestamp |
| `updatedAt` | `Instant` | Update timestamp |
| `designCase` | `DesignCase` | Owning design case |
| `material` | `GearMaterial` | Chosen material |
| `shaftForces` | `List<Module3ShaftForce>` | Child rows with force vectors |

### Why this entity exists
This is the main backend source of truth for the Module 3 result screen.
It keeps all critical design outputs in one place and allows the user to reopen a saved calculation later.

---

## 3.3 `Module3ShaftForce`

### Role
Stores the force vector for each relevant shaft produced by Module 3.

### Current table name
```text
module3_shaft_force
```

### Attributes

| Field | Type | Meaning |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `shaftCode` | `ShaftCode` | Current values used by Module 3 are `SHAFT_1` and `SHAFT_2` |
| `ftN` | `BigDecimal` | Tangential force |
| `frN` | `BigDecimal` | Radial force |
| `faN` | `BigDecimal` | Axial force |
| `createdAt` | `Instant` | Creation timestamp |
| `module3Result` | `Module3Result` | Parent Module 3 result |

### Unique rule
There is a unique constraint on:
- `module3_result_id`
- `shaft_code`

This ensures one force vector per shaft per Module 3 result.

---

## 3.4 `DesignCase` fields relevant to Module 3

Even though `DesignCase` belongs to the whole product flow, these fields matter directly to Module 3:

| Field | Meaning for Module 3 |
| --- | --- |
| `id` | Root case identifier used by API |
| `caseCode` | Stable business key |
| `caseName` | Display label |
| `serviceLifeHours` | Saved `Lh` fallback if Module 3 request omits it |
| `status` | Becomes `MODULE3_COMPLETED` after successful Module 3 save |
| `module1Result` | Required upstream result |
| `module3Result` | Current saved Module 3 result |

---

# 4. Current Calculation Notes and Assumptions

The current Module 3 implementation is intentionally **usable and consistent**, but not yet the final full mechanical-standard implementation.

## Current implemented assumptions

1. Module 3 depends on a saved `Module1Result`
2. If `T1`, `n1`, or `U2` is missing from the request, it is inherited from Module 1
3. If `serviceLifeHours` is not provided and not saved in the case, backend defaults to `43200`
4. Material properties come from seeded `GearMaterial` records
5. Tooth-count selection uses standard modules and integer teeth, then records actual ratio error
6. Force vectors are returned for `SHAFT_1` and `SHAFT_2`

## Current placeholders / simplifications

1. **Allowable stress model is simplified**
   - life factors and stress derivation are implemented as a practical approximation
   - not yet a full textbook/table-driven final method

2. **Tooth form factor is approximated**
   - not yet backed by a full lookup table

3. **Inherited `U2` may still be a Module 1 placeholder**
   - because Module 1 currently stores `U2 = 3.14` as a temporary upstream split when not overridden

These points are intentionally surfaced in `calculationNotes`.

---

# 5. Practical Test Cases for Module 3

## Precondition for most tests

Before testing Module 3, create a design case through Module 1.

### Step A — create a Module 1 case

**Request**

```http
POST /api/v1/module-1/calculate
Content-Type: application/json

{
  "requiredPowerKw": 5.5,
  "requiredOutputRpm": 70,
  "caseCode": "CASE-M3-001",
  "caseName": "Module 3 Test Seed"
}
```

**Expected important output**
- HTTP `200`
- response contains `caseInfo.designCaseId`
- response contains `transmissionRatios.bevelRatioU2`
- response contains shaft state `SHAFT_1`

Use that `designCaseId` in the Module 3 test cases below.

---

## TC-01 — get materials list

### Purpose
Verify reference material data is available.

### Request
```http
GET /api/v1/module-3/materials
```

### Expected
- status `200`
- response is a non-empty JSON array
- each object contains:
  - `materialId`
  - `materialCode`
  - `hbMin`
  - `hbMax`
  - `sigmaBMpa`
  - `sigmaChMpa`

---

## TC-02 — calculate Module 3 using inherited Module 1 values

### Purpose
Verify the happy path using only `designCaseId` and `materialId`.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 8,
  "materialId": 1
}
```

### Expected
- status `200`
- `caseInfo.status = MODULE3_COMPLETED`
- `inputSummary.inputT1Nmm` comes from Module 1 shaft 1 torque
- `inputSummary.inputN1Rpm` comes from Module 1 shaft 1 rpm
- `inputSummary.inputU2` comes from Module 1 result
- `inputSummary.serviceLifeHours = 43200.000000` if case had no saved service life yet
- `selectedMaterial.materialId = 1`
- `gearGeometry.teethZ1` and `gearGeometry.teethZ2` are positive integers
- `shaftForces` contains exactly 2 rows:
  - `SHAFT_1`
  - `SHAFT_2`
- `stressCheck.contactStressPass` is boolean
- `stressCheck.bendingStressPass` is boolean
- `calculationNotes` explains inherited/default values

---

## TC-03 — calculate Module 3 with explicit engineering overrides

### Purpose
Verify request overrides are respected instead of Module 1 defaults.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 8,
  "inputT1Nmm": 83851.991,
  "inputN1Rpm": 812,
  "inputU2": 3.14,
  "serviceLifeHours": 43200,
  "materialId": 1
}
```

### Expected
- status `200`
- `inputSummary` exactly matches request values
- `calculationNotes` says input torque/speed/U2 were overridden directly in the request
- result is persisted and can be reopened later

---

## TC-04 — reopen saved Module 3 result

### Purpose
Verify persistence and reopen behavior.

### Request
```http
GET /api/v1/module-3/history/8
```

### Expected
- status `200`
- response shape is the same as calculate response
- `resultInfo.resultId` exists
- `caseInfo.designCaseId = 8`
- geometry, stresses, and shaft forces are present

---

## TC-05 — calculate again for same design case should replace old Module 3 result

### Purpose
Verify recalculation replaces the previous saved result for that case.

### Steps
1. Call `POST /calculate` with material `1`
2. Call `POST /calculate` again for the same `designCaseId` with material `2`
3. Call `GET /history/{designCaseId}`

### Expected
- both calculate calls return `200`
- final reopened result reflects the second request
- case still has only one active saved `Module3Result`
- selected material in reopened response equals material `2`

---

## TC-06 — invalid design case ID

### Purpose
Verify not-found handling.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 999999,
  "materialId": 1
}
```

### Expected
- status `404`
- backend message indicates design case not found

---

## TC-07 — invalid material ID

### Purpose
Verify material lookup validation.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 8,
  "materialId": 999999
}
```

### Expected
- status `404`
- backend message indicates gear material not found

---

## TC-08 — call Module 3 for a design case that has no Module 1 result

### Purpose
Verify Module 1 prerequisite enforcement.

### Setup
Create a `DesignCase` in DB without running Module 1, or use a case known not to have a `Module1Result`.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 15,
  "materialId": 1
}
```

### Expected
- status `422`
- error indicates Module 1 result is required before Module 3

---

## TC-09 — invalid negative engineering override

### Purpose
Verify numeric validation.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 8,
  "inputT1Nmm": -1,
  "materialId": 1
}
```

### Expected
- status `400` or `422` depending on whether validation is caught at request-binding level or service level
- request must not succeed

---

## TC-10 — confirm saved service life reuse

### Purpose
Verify `serviceLifeHours` fallback from `DesignCase`.

### Steps
1. Call Module 3 with:
   ```json
   {
     "designCaseId": 8,
     "serviceLifeHours": 50000,
     "materialId": 1
   }
   ```
2. Call Module 3 again for same case without `serviceLifeHours`

### Expected
- second response uses `inputSummary.serviceLifeHours = 50000.000000`
- notes say service life was reused from the saved design case

---

# 6. Suggested Manual Test Script

## 6.1 Create seed case in Module 1

```bash
curl -X POST http://localhost:8080/api/v1/module-1/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "requiredPowerKw": 5.5,
    "requiredOutputRpm": 70,
    "caseCode": "CASE-M3-001",
    "caseName": "Module 3 Test Seed"
  }'
```

## 6.2 Get materials

```bash
curl http://localhost:8080/api/v1/module-3/materials
```

## 6.3 Calculate Module 3 using inherited upstream values

```bash
curl -X POST http://localhost:8080/api/v1/module-3/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "designCaseId": 8,
    "materialId": 1
  }'
```

## 6.4 Reopen saved result

```bash
curl http://localhost:8080/api/v1/module-3/history/8
```

---

# 7. Frontend Integration Notes

- Always obtain a valid `designCaseId` from Module 1 first
- Use `/materials` to populate the material selector
- The calculate response is already structured for a result screen:
  - input summary
  - selected material
  - allowable stress section
  - gear geometry
  - validation flags
  - shaft forces
  - notes/warnings
- Show `calculationNotes` in the UI because they reveal inherited values and active placeholders
- If the user recalculates Module 1 for the same case, any old Module 3 result is invalidated by backend logic and must be recalculated

---

# 8. Current Seeded Materials

These are inserted by `DataInitializer` if missing:

| materialCode | materialName | heatTreatment | hbMin | hbMax | sigmaBMpa | sigmaChMpa |
| --- | --- | --- | ---: | ---: | ---: | ---: |
| `C40XH_QT` | Steel C40XH | Quenched and tempered | 235 | 262 | 850 | 650 |
| `40CR_N` | Steel 40Cr | Normalized | 207 | 241 | 780 | 540 |
| `C45_N` | Steel C45 | Normalized | 179 | 207 | 600 | 355 |

---

# 9. Summary

Module 3 currently supports:
- material lookup
- saved-case-based bevel gear calculation
- inherited upstream values from Module 1
- optional engineering overrides
- persisted result reopen
- geometry output
- stress validation flags
- shaft-force output
- notes documenting assumptions and placeholder behavior

The implementation is ready for integration and testing in the current product phase, while still clearly marking the remaining mechanical-detail approximations.
