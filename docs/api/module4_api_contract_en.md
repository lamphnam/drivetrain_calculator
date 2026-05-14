# Module 4 API Contract (Mobile)

This document helps mobile developers integrate Module 4 (straight spur gear calculation) quickly and correctly without reading backend code.

## 1. Overview

- Module 4 calculates straight spur gear geometry, derived factors, stress checks, and shaft forces.
- Flow position: Module 1 -> (Module 3 optional) -> Module 4.
- Required prerequisite: Module 1 result must exist for the same design case.
- Optional dependency: Module 3 result can supply stress defaults.
- Module 4 result is saved per `designCaseId` and overwritten on re-calc.

## 2. Endpoints

### Module 4

- POST `/api/v1/module-4/calculate`
- GET `/api/v1/module-4/history/{designCaseId}`

### Full-flow (Module 1 -> Module 3 -> Module 4)

- POST `/api/v1/drivetrain/full-flow/calculate`
- GET `/api/v1/drivetrain/full-flow/history/{designCaseId}`

**When to call Module 4 directly**

- When you already have `designCaseId` and want to re-calculate or override Module 4 inputs.

**When to call full-flow**

- When you want a new case and all three module results in one call.

## 3. Request contract (POST /api/v1/module-4/calculate)

### 3.1 Request fields (Module4CalculationRequest)

| Field                          | Type                | Required | Validation      | Description                                     | Unit  |
| ------------------------------ | ------------------- | -------- | --------------- | ----------------------------------------------- | ----- |
| designCaseId                   | number (Long)       | Yes      | > 0             | Design case ID that already has Module 1 result | -     |
| inputT2Nmm                     | number (BigDecimal) | No       | > 0 if provided | Torque T2 at Shaft 2 (override)                 | N.mm  |
| inputN2Rpm                     | number (BigDecimal) | No       | > 0 if provided | Speed n2 at Shaft 2 (override)                  | rpm   |
| inputU3                        | number (BigDecimal) | No       | > 0 if provided | Spur ratio U3 (override)                        | ratio |
| allowableContactStressMpa      | number (BigDecimal) | No       | > 0 if provided | Allowable contact stress                        | MPa   |
| allowableBendingStressGear1Mpa | number (BigDecimal) | No       | > 0 if provided | Allowable bending stress for gear 1             | MPa   |
| allowableBendingStressGear2Mpa | number (BigDecimal) | No       | > 0 if provided | Allowable bending stress for gear 2             | MPa   |

### 3.2 Inheritance / default rules

- `inputT2Nmm`: if omitted, inherited from Module 1, Shaft 2 (`torqueNmm`).
- `inputN2Rpm`: if omitted, inherited from Module 1, Shaft 2 (`rpm`).
- `inputU3`: if omitted, inherited from Module 1 (`spurGearRatioU3`).
- Stress allowables:
  - If request contains at least one stress field, backend uses request values when present; missing stress fields fall back to backend defaults (no Module 3 inheritance).
  - If request contains no stress fields, backend uses Module 3 result (`allowableContactStressMpa`, `allowableBendingStressMpa`) when available.
    - Both gear 1 and gear 2 bending allowables come from Module 3 `allowableBendingStressMpa`.
  - If Module 3 result is missing, backend defaults apply: `sigmaH=600 MPa`, `sigmaF1=260 MPa`, `sigmaF2=260 MPa`.

### 3.3 Minimal request example

```json
{
  "designCaseId": 1201
}
```

### 3.4 Full override request example

```json
{
  "designCaseId": 1201,
  "inputT2Nmm": 185000,
  "inputN2Rpm": 980,
  "inputU3": 3.2,
  "allowableContactStressMpa": 650,
  "allowableBendingStressGear1Mpa": 280,
  "allowableBendingStressGear2Mpa": 260
}
```

## 4. Response contract (Module4CalculationResponse)

### 4.1 Top-level structure

```text
resultInfo, caseInfo, inputSummary, spurGearGeometry, derivedFactors,
stressCheck, shaftForces[], calculationNotes[]
```

### 4.2 Block details

**ResultInfo**

| Field     | Type              | Meaning            | Unit | Nullable |
| --------- | ----------------- | ------------------ | ---- | -------- |
| resultId  | number            | Module 4 result ID | -    | No       |
| createdAt | string (ISO-8601) | Created time       | -    | No       |
| updatedAt | string (ISO-8601) | Updated time       | -    | No       |

**CaseInfo**

| Field        | Type   | Meaning                        | Unit | Nullable    |
| ------------ | ------ | ------------------------------ | ---- | ----------- |
| designCaseId | number | Case ID                        | -    | No          |
| caseCode     | string | Case code                      | -    | Can be null |
| caseName     | string | Case name                      | -    | Can be null |
| status       | string | Case status (DesignCaseStatus) | -    | No          |

`status` values: `DRAFT`, `MODULE1_COMPLETED`, `MODULE3_COMPLETED`, `MODULE4_COMPLETED`, `FAILED`.

**InputSummary**

| Field                          | Type   | Meaning                         | Unit  | Nullable |
| ------------------------------ | ------ | ------------------------------- | ----- | -------- |
| inputT2Nmm                     | number | Torque T2 used for calc         | N.mm  | No       |
| inputN2Rpm                     | number | Speed n2 used for calc          | rpm   | No       |
| inputU3                        | number | Ratio U3 used for calc          | ratio | No       |
| allowableContactStressMpa      | number | Allowable contact stress        | MPa   | No       |
| allowableBendingStressGear1Mpa | number | Allowable bending stress gear 1 | MPa   | No       |
| allowableBendingStressGear2Mpa | number | Allowable bending stress gear 2 | MPa   | No       |

**SpurGearGeometry**

| Field              | Type         | Meaning               | Unit  | Nullable |
| ------------------ | ------------ | --------------------- | ----- | -------- |
| centerDistanceAwMm | number       | Center distance aw    | mm    | No       |
| moduleMSelected    | number       | Selected module       | mm    | No       |
| teethZ1            | number (int) | Teeth count gear 1    | -     | No       |
| teethZ2            | number (int) | Teeth count gear 2    | -     | No       |
| actualRatioU3      | number       | Actual ratio          | ratio | No       |
| ratioErrorPercent  | number       | Ratio error percent   | %     | No       |
| diameterDw1Mm      | number       | Pitch diameter gear 1 | mm    | No       |
| diameterDw2Mm      | number       | Pitch diameter gear 2 | mm    | No       |
| widthBwMm          | number       | Face width            | mm    | No       |

**DerivedFactors**

| Field        | Type   | Meaning              | Unit  | Nullable |
| ------------ | ------ | -------------------- | ----- | -------- |
| epsilonAlpha | number | Contact ratio factor | ratio | No       |
| zEpsilon     | number | Z epsilon factor     | ratio | No       |
| yEpsilon     | number | Y epsilon factor     | ratio | No       |
| yF1          | number | YF1 factor           | ratio | No       |
| yF2          | number | YF2 factor           | ratio | No       |
| loadFactorKh | number | Load factor KH       | ratio | No       |
| loadFactorKf | number | Load factor KF       | ratio | No       |

**StressCheck**

| Field                  | Type    | Meaning                    | Unit    | Nullable |
| ---------------------- | ------- | -------------------------- | ------- | -------- |
| sigmaHMpa              | number  | Contact stress             | MPa     | No       |
| sigmaF1Mpa             | number  | Bending stress gear 1      | MPa     | No       |
| sigmaF2Mpa             | number  | Bending stress gear 2      | MPa     | No       |
| contactStressPass      | boolean | Contact stress pass        | boolean | No       |
| bendingStressGear1Pass | boolean | Bending stress gear 1 pass | boolean | No       |
| bendingStressGear2Pass | boolean | Bending stress gear 2 pass | boolean | No       |

**ShaftForceSummary** (items in `shaftForces`)

| Field      | Type   | Meaning                           | Unit | Nullable      |
| ---------- | ------ | --------------------------------- | ---- | ------------- |
| shaftCode  | string | Shaft code (`SHAFT_2`, `SHAFT_3`) | -    | No            |
| shaftLabel | string | Display label (e.g., "Shaft 2")   | -    | No            |
| ftN        | number | Tangential force                  | N    | No            |
| frN        | number | Radial force                      | N    | No            |
| faN        | number | Axial force                       | N    | No (always 0) |

**calculationNotes**

- List of calculation notes (string). Can be empty.

### 4.3 Field -> UI label mapping

| Field path                          | Suggested UI label | Notes                   |
| ----------------------------------- | ------------------ | ----------------------- |
| spurGearGeometry.centerDistanceAwMm | Center distance aw | Key value               |
| spurGearGeometry.moduleMSelected    | Module m           | Key value               |
| spurGearGeometry.teethZ1            | Teeth Z1           | Key value               |
| spurGearGeometry.teethZ2            | Teeth Z2           | Key value               |
| spurGearGeometry.actualRatioU3      | Actual ratio U3    | Compare to requested U3 |
| spurGearGeometry.ratioErrorPercent  | Ratio error        | Display percent         |
| stressCheck.sigmaHMpa               | Sigma H            | Contact stress          |
| stressCheck.sigmaF1Mpa              | Sigma F1           | Bending stress gear 1   |
| stressCheck.sigmaF2Mpa              | Sigma F2           | Bending stress gear 2   |
| stressCheck.contactStressPass       | Contact pass       | Pass/fail badge         |
| stressCheck.bendingStressGear1Pass  | Gear 1 pass        | Pass/fail badge         |
| stressCheck.bendingStressGear2Pass  | Gear 2 pass        | Pass/fail badge         |
| shaftForces[].ftN                   | Ft (tangential)    | Per shaft               |
| shaftForces[].frN                   | Fr (radial)        | Per shaft               |
| shaftForces[].faN                   | Fa (axial)         | Always 0                |

### 4.4 Field -> unit mapping

| Field path                                  | Unit    |
| ------------------------------------------- | ------- |
| inputSummary.inputT2Nmm                     | N.mm    |
| inputSummary.inputN2Rpm                     | rpm     |
| inputSummary.inputU3                        | ratio   |
| inputSummary.allowableContactStressMpa      | MPa     |
| inputSummary.allowableBendingStressGear1Mpa | MPa     |
| inputSummary.allowableBendingStressGear2Mpa | MPa     |
| spurGearGeometry.centerDistanceAwMm         | mm      |
| spurGearGeometry.moduleMSelected            | mm      |
| spurGearGeometry.diameterDw1Mm              | mm      |
| spurGearGeometry.diameterDw2Mm              | mm      |
| spurGearGeometry.widthBwMm                  | mm      |
| spurGearGeometry.actualRatioU3              | ratio   |
| spurGearGeometry.ratioErrorPercent          | %       |
| derivedFactors.\*                           | ratio   |
| stressCheck.sigmaHMpa                       | MPa     |
| stressCheck.sigmaF1Mpa                      | MPa     |
| stressCheck.sigmaF2Mpa                      | MPa     |
| stressCheck.\*Pass                          | boolean |
| shaftForces[].ftN                           | N       |
| shaftForces[].frN                           | N       |
| shaftForces[].faN                           | N       |

### 4.5 Summary card priority fields

- `centerDistanceAwMm`, `moduleMSelected`, `teethZ1`, `teethZ2`.
- `actualRatioU3`, `ratioErrorPercent`.
- `sigmaHMpa`, `sigmaF1Mpa`, `sigmaF2Mpa`.
- `contactStressPass`, `bendingStressGear1Pass`, `bendingStressGear2Pass`.

## 5. Example responses

### 5.1 Success - Module 4 calculate

```json
{
  "resultInfo": {
    "resultId": 4502,
    "createdAt": "2026-05-14T13:22:11Z",
    "updatedAt": "2026-05-14T13:22:11Z"
  },
  "caseInfo": {
    "designCaseId": 1201,
    "caseCode": "FULL-TEST-001",
    "caseName": "Full Flow Test Case",
    "status": "MODULE4_COMPLETED"
  },
  "inputSummary": {
    "inputT2Nmm": 185000.0,
    "inputN2Rpm": 980.0,
    "inputU3": 3.2,
    "allowableContactStressMpa": 650.0,
    "allowableBendingStressGear1Mpa": 280.0,
    "allowableBendingStressGear2Mpa": 260.0
  },
  "spurGearGeometry": {
    "centerDistanceAwMm": 121.43,
    "moduleMSelected": 2.5,
    "teethZ1": 19,
    "teethZ2": 61,
    "actualRatioU3": 3.210526,
    "ratioErrorPercent": 0.329563,
    "diameterDw1Mm": 57.8,
    "diameterDw2Mm": 185.1,
    "widthBwMm": 30.36
  },
  "derivedFactors": {
    "epsilonAlpha": 1.402,
    "zEpsilon": 0.944,
    "yEpsilon": 0.713,
    "yF1": 3.8,
    "yF2": 3.6,
    "loadFactorKh": 1.061,
    "loadFactorKf": 1.177
  },
  "stressCheck": {
    "sigmaHMpa": 512.4,
    "sigmaF1Mpa": 189.7,
    "sigmaF2Mpa": 179.7,
    "contactStressPass": true,
    "bendingStressGear1Pass": true,
    "bendingStressGear2Pass": true
  },
  "shaftForces": [
    {
      "shaftCode": "SHAFT_2",
      "shaftLabel": "Shaft 2",
      "ftN": 6400.0,
      "frN": 2331.0,
      "faN": 0.0
    },
    {
      "shaftCode": "SHAFT_3",
      "shaftLabel": "Shaft 3",
      "ftN": 6400.0,
      "frN": 2331.0,
      "faN": 0.0
    }
  ],
  "calculationNotes": [
    "Input torque T2 was inherited from Module 1 shaft state SHAFT_2.",
    "Input speed n2 was inherited from Module 1 shaft state SHAFT_2.",
    "Input spur ratio U3 was inherited from stored Module 1 ratio U3.",
    "Allowable stresses were provided directly in the Module 4 request payload.",
    "Selected module is the largest standard module within the valid range [0.01*aw, 0.02*aw].",
    "Actual ratio error versus requested U3: 0.329563%.",
    "Axial force Fa = 0 because straight spur gear teeth produce no axial thrust."
  ]
}
```

### 5.2 Success - Module 4 history

```json
{
  "resultInfo": {
    "resultId": 4502,
    "createdAt": "2026-05-14T13:22:11Z",
    "updatedAt": "2026-05-14T13:25:00Z"
  },
  "caseInfo": {
    "designCaseId": 1201,
    "caseCode": "FULL-TEST-001",
    "caseName": "Full Flow Test Case",
    "status": "MODULE4_COMPLETED"
  },
  "inputSummary": {
    "inputT2Nmm": 185000.0,
    "inputN2Rpm": 980.0,
    "inputU3": 3.2,
    "allowableContactStressMpa": 650.0,
    "allowableBendingStressGear1Mpa": 280.0,
    "allowableBendingStressGear2Mpa": 260.0
  },
  "spurGearGeometry": {
    "centerDistanceAwMm": 121.43,
    "moduleMSelected": 2.5,
    "teethZ1": 19,
    "teethZ2": 61,
    "actualRatioU3": 3.210526,
    "ratioErrorPercent": 0.329563,
    "diameterDw1Mm": 57.8,
    "diameterDw2Mm": 185.1,
    "widthBwMm": 30.36
  },
  "derivedFactors": {
    "epsilonAlpha": 1.402,
    "zEpsilon": 0.944,
    "yEpsilon": 0.713,
    "yF1": 3.8,
    "yF2": 3.6,
    "loadFactorKh": 1.061,
    "loadFactorKf": 1.177
  },
  "stressCheck": {
    "sigmaHMpa": 512.4,
    "sigmaF1Mpa": 189.7,
    "sigmaF2Mpa": 179.7,
    "contactStressPass": true,
    "bendingStressGear1Pass": true,
    "bendingStressGear2Pass": true
  },
  "shaftForces": [
    {
      "shaftCode": "SHAFT_2",
      "shaftLabel": "Shaft 2",
      "ftN": 6400.0,
      "frN": 2331.0,
      "faN": 0.0
    },
    {
      "shaftCode": "SHAFT_3",
      "shaftLabel": "Shaft 3",
      "ftN": 6400.0,
      "frN": 2331.0,
      "faN": 0.0
    }
  ],
  "calculationNotes": [
    "Input torque T2 was inherited from Module 1 shaft state SHAFT_2.",
    "Input speed n2 was inherited from Module 1 shaft state SHAFT_2.",
    "Input spur ratio U3 was inherited from stored Module 1 ratio U3.",
    "Allowable stresses were inherited from the Module 3 result for the same design case.",
    "Selected module is the largest standard module within the valid range [0.01*aw, 0.02*aw].",
    "Actual ratio error versus requested U3: 0.329563%.",
    "Axial force Fa = 0 because straight spur gear teeth produce no axial thrust."
  ]
}
```

**Full-flow history note:** `module4Result` can be `null` if Module 4 has not been calculated; use `warnings` in the full-flow response to display a proper message.

## 6. Error contract

Exact global error wrapper not found in inspected files; verify with backend once app is running.

| Scenario                 | HTTP status (likely) | Cause                                                       | Mobile handling suggestion     |
| ------------------------ | -------------------- | ----------------------------------------------------------- | ------------------------------ |
| Missing `designCaseId`   | 400                  | Bean validation failure                                     | Show "Design case is required" |
| `designCaseId` not found | 404                  | Module4DesignCaseNotFoundException                          | Show "Case not found"          |
| Module 1 result missing  | 422                  | Module4PrerequisiteMissingException                         | Ask user to run Module 1 first |
| Shaft 2 state missing    | 422                  | Module4PrerequisiteMissingException                         | Inform upstream data missing   |
| Non-positive input value | 400 or 422           | Bean validation (400) or InvalidModule4InputException (422) | Ask user to input value > 0    |
| Module 4 history missing | 404                  | Module4ResultNotFoundException                              | Show "No Module 4 result"      |

## 7. Mobile integration flow

**A. Normal full calculation**

1. POST `/api/v1/drivetrain/full-flow/calculate`.
2. Read `module4Result` from response.
3. Store `caseSummary.designCaseId`.

**B. Existing design case**

1. POST `/api/v1/module-4/calculate` with `designCaseId` only or with overrides.
2. If needed, GET `/api/v1/module-4/history/{designCaseId}`.

**C. Read-only detail screen**

1. GET `/api/v1/module-4/history/{designCaseId}`.

## 8. TypeScript models

```ts
export type DesignCaseStatus =
  | "DRAFT"
  | "MODULE1_COMPLETED"
  | "MODULE3_COMPLETED"
  | "MODULE4_COMPLETED"
  | "FAILED";

export type ShaftCode =
  | "MOTOR"
  | "SHAFT_1"
  | "SHAFT_2"
  | "SHAFT_3"
  | "DRUM_SHAFT";

export interface Module4CalculationRequest {
  designCaseId: number;
  inputT2Nmm?: number;
  inputN2Rpm?: number;
  inputU3?: number;
  allowableContactStressMpa?: number;
  allowableBendingStressGear1Mpa?: number;
  allowableBendingStressGear2Mpa?: number;
}

export interface Module4CalculationResponse {
  resultInfo: Module4ResultInfo;
  caseInfo: Module4CaseInfo;
  inputSummary: Module4InputSummary;
  spurGearGeometry: Module4SpurGearGeometry;
  derivedFactors: Module4DerivedFactors;
  stressCheck: Module4StressCheck;
  shaftForces: Module4ShaftForceSummary[];
  calculationNotes: string[];
}

export interface Module4ResultInfo {
  resultId: number;
  createdAt: string;
  updatedAt: string;
}

export interface Module4CaseInfo {
  designCaseId: number;
  caseCode: string | null;
  caseName: string | null;
  status: DesignCaseStatus;
}

export interface Module4InputSummary {
  inputT2Nmm: number;
  inputN2Rpm: number;
  inputU3: number;
  allowableContactStressMpa: number;
  allowableBendingStressGear1Mpa: number;
  allowableBendingStressGear2Mpa: number;
}

export interface Module4SpurGearGeometry {
  centerDistanceAwMm: number;
  moduleMSelected: number;
  teethZ1: number;
  teethZ2: number;
  actualRatioU3: number;
  ratioErrorPercent: number;
  diameterDw1Mm: number;
  diameterDw2Mm: number;
  widthBwMm: number;
}

export interface Module4DerivedFactors {
  epsilonAlpha: number;
  zEpsilon: number;
  yEpsilon: number;
  yF1: number;
  yF2: number;
  loadFactorKh: number;
  loadFactorKf: number;
}

export interface Module4StressCheck {
  sigmaHMpa: number;
  sigmaF1Mpa: number;
  sigmaF2Mpa: number;
  contactStressPass: boolean;
  bendingStressGear1Pass: boolean;
  bendingStressGear2Pass: boolean;
}

export interface Module4ShaftForceSummary {
  shaftCode: ShaftCode;
  shaftLabel: string;
  ftN: number;
  frN: number;
  faN: number;
}
```

## 9. Kotlin models (Android)

If you need strict precision (calc/storage), use `BigDecimal` or `String`. For display-only UI, `Double` is acceptable.

```kotlin
import java.math.BigDecimal

enum class ShaftCode {
    MOTOR,
    SHAFT_1,
    SHAFT_2,
    SHAFT_3,
    DRUM_SHAFT
}

data class Module4CalculationRequest(
    val designCaseId: Long,
    val inputT2Nmm: BigDecimal? = null,
    val inputN2Rpm: BigDecimal? = null,
    val inputU3: BigDecimal? = null,
    val allowableContactStressMpa: BigDecimal? = null,
    val allowableBendingStressGear1Mpa: BigDecimal? = null,
    val allowableBendingStressGear2Mpa: BigDecimal? = null
)

data class Module4CalculationResponse(
    val resultInfo: ResultInfo,
    val caseInfo: CaseInfo,
    val inputSummary: InputSummary,
    val spurGearGeometry: SpurGearGeometry,
    val derivedFactors: DerivedFactors,
    val stressCheck: StressCheck,
    val shaftForces: List<ShaftForceSummary>,
    val calculationNotes: List<String>
)

data class ResultInfo(
    val resultId: Long,
    val createdAt: String,
    val updatedAt: String
)

data class CaseInfo(
    val designCaseId: Long,
    val caseCode: String?,
    val caseName: String?,
    val status: String
)

data class InputSummary(
    val inputT2Nmm: BigDecimal,
    val inputN2Rpm: BigDecimal,
    val inputU3: BigDecimal,
    val allowableContactStressMpa: BigDecimal,
    val allowableBendingStressGear1Mpa: BigDecimal,
    val allowableBendingStressGear2Mpa: BigDecimal
)

data class SpurGearGeometry(
    val centerDistanceAwMm: BigDecimal,
    val moduleMSelected: BigDecimal,
    val teethZ1: Int,
    val teethZ2: Int,
    val actualRatioU3: BigDecimal,
    val ratioErrorPercent: BigDecimal,
    val diameterDw1Mm: BigDecimal,
    val diameterDw2Mm: BigDecimal,
    val widthBwMm: BigDecimal
)

data class DerivedFactors(
    val epsilonAlpha: BigDecimal,
    val zEpsilon: BigDecimal,
    val yEpsilon: BigDecimal,
    val yF1: BigDecimal,
    val yF2: BigDecimal,
    val loadFactorKh: BigDecimal,
    val loadFactorKf: BigDecimal
)

data class StressCheck(
    val sigmaHMpa: BigDecimal,
    val sigmaF1Mpa: BigDecimal,
    val sigmaF2Mpa: BigDecimal,
    val contactStressPass: Boolean,
    val bendingStressGear1Pass: Boolean,
    val bendingStressGear2Pass: Boolean
)

data class ShaftForceSummary(
    val shaftCode: ShaftCode,
    val shaftLabel: String,
    val ftN: BigDecimal,
    val frN: BigDecimal,
    val faN: BigDecimal
)
```

## 10. UI notes

- Suggested order: Input summary -> Spur gear geometry -> Stress check -> Shaft forces -> Calculation notes.
- Rounding for display: mm and MPa to 2-3 decimals; ratio to 4 decimals; ratioErrorPercent to 2 decimals.
- `contactStressPass`, `bendingStressGear1Pass`, `bendingStressGear2Pass`: show clear PASS/FAIL badges.
- Show `calculationNotes` in detail view; keep summary clean.
- `faN` is always 0 for straight spur gear; can be hidden or noted.
- `shaftForces` are sorted by Shaft 2 then Shaft 3 in backend response.

## 11. QA checklist for mobile

- Can calculate with `designCaseId` only.
- Can calculate with full override request.
- Can open Module 4 history by `designCaseId`.
- Handles missing Module 1 prerequisite.
- Handles `*Pass` booleans when false.
- Handles full-flow history where `module4Result` is null and `warnings` is present.
