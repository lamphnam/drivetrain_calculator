# Module 4 API Contract (Mobile)

Tai lieu nay danh cho mobile developer tich hop Module 4 (tinh toan banh rang tru thang - spur gear) nhanh va dung, khong can doc code backend.

## 1. Tong quan

- Module 4 tinh toan bo banh rang tru thang: hinh hoc, he so, kiem tra ung suat va luc tren truc.
- Vi tri trong flow: Module 1 -> (Module 3 tuy chon) -> Module 4.
- Bat buoc: phai co Module 1 result truoc (de lay Torque va RPM cua Shaft 2).
- Tuy chon: Module 3 result co the cung cap stress allowables mac dinh.
- Ket qua Module 4 luu theo `designCaseId` va se ghi de neu tinh lai.

## 2. Endpoints

### Module 4

- POST `/api/v1/module-4/calculate`
- GET `/api/v1/module-4/history/{designCaseId}`

### Full-flow (Module 1 -> Module 3 -> Module 4)

- POST `/api/v1/drivetrain/full-flow/calculate`
- GET `/api/v1/drivetrain/full-flow/history/{designCaseId}`

**Khi nao goi Module 4 truc tiep?**

- Khi UI da co `designCaseId` va muon tinh lai hoac override input rieng cho Module 4.

**Khi nao goi full-flow?**

- Khi muon tinh toan day du tu dau (tao case moi, lay ket qua tat ca module trong 1 call).

## 3. Request contract (POST /api/v1/module-4/calculate)

### 3.1 Truong request (Module4CalculationRequest)

| Field                          | Type                | Required | Validation | Mo ta                             | Don vi |
| ------------------------------ | ------------------- | -------- | ---------- | --------------------------------- | ------ |
| designCaseId                   | number (Long)       | Yes      | > 0        | ID case da co Module 1 result     | -      |
| inputT2Nmm                     | number (BigDecimal) | No       | > 0        | Torque T2 tren Shaft 2 (override) | N.mm   |
| inputN2Rpm                     | number (BigDecimal) | No       | > 0        | Toc do n2 tren Shaft 2 (override) | rpm    |
| inputU3                        | number (BigDecimal) | No       | > 0        | Ty so truyen U3 (override)        | ratio  |
| allowableContactStressMpa      | number (BigDecimal) | No       | > 0        | Ung suat tiep xuc cho phep        | MPa    |
| allowableBendingStressGear1Mpa | number (BigDecimal) | No       | > 0        | Ung suat uon cho phep banh 1      | MPa    |
| allowableBendingStressGear2Mpa | number (BigDecimal) | No       | > 0        | Ung suat uon cho phep banh 2      | MPa    |

### 3.2 Quy tac ke thua / default

- `inputT2Nmm`: neu khong gui -> lay tu Module 1, Shaft 2 (`torqueNmm`).
- `inputN2Rpm`: neu khong gui -> lay tu Module 1, Shaft 2 (`rpm`).
- `inputU3`: neu khong gui -> lay tu Module 1 (`spurGearRatioU3`).
- Stress allowables:
  - Neu request co it nhat 1 truong stress: backend dung gia tri request neu co; cac truong stress thieu se dung mac dinh backend (khong lay tu Module 3).
  - Neu request khong co truong stress nao: backend lay tu Module 3 result (`allowableContactStressMpa`, `allowableBendingStressMpa`) neu co;
    - Gia tri bending cho Gear1 va Gear2 deu lay tu `allowableBendingStressMpa` cua Module 3.
  - Neu khong co Module 3 result: dung mac dinh backend `sigmaH=600 MPa`, `sigmaF1=260 MPa`, `sigmaF2=260 MPa`.

### 3.3 Vi du request toi thieu

```json
{
  "designCaseId": 1201
}
```

### 3.4 Vi du request override day du

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

### 4.1 Cau truc tong quan

```text
resultInfo, caseInfo, inputSummary, spurGearGeometry, derivedFactors,
stressCheck, shaftForces[], calculationNotes[]
```

### 4.2 Chi tiet cac block

**ResultInfo**

| Field     | Type              | Meaning             | Unit | Nullable |
| --------- | ----------------- | ------------------- | ---- | -------- |
| resultId  | number            | ID ket qua Module 4 | -    | No       |
| createdAt | string (ISO-8601) | Thoi gian tao       | -    | No       |
| updatedAt | string (ISO-8601) | Thoi gian cap nhat  | -    | No       |

**CaseInfo**

| Field        | Type   | Meaning                            | Unit | Nullable    |
| ------------ | ------ | ---------------------------------- | ---- | ----------- |
| designCaseId | number | ID case                            | -    | No          |
| caseCode     | string | Ma case                            | -    | Co the null |
| caseName     | string | Ten case                           | -    | Co the null |
| status       | string | Trang thai case (DesignCaseStatus) | -    | No          |

Gia tri `status` co the la: `DRAFT`, `MODULE1_COMPLETED`, `MODULE3_COMPLETED`, `MODULE4_COMPLETED`, `FAILED`.

**InputSummary**

| Field                          | Type   | Meaning                      | Unit  | Nullable |
| ------------------------------ | ------ | ---------------------------- | ----- | -------- |
| inputT2Nmm                     | number | Torque T2 da dung de tinh    | N.mm  | No       |
| inputN2Rpm                     | number | Toc do n2 da dung de tinh    | rpm   | No       |
| inputU3                        | number | Ty so U3 da dung de tinh     | ratio | No       |
| allowableContactStressMpa      | number | Ung suat tiep xuc cho phep   | MPa   | No       |
| allowableBendingStressGear1Mpa | number | Ung suat uon cho phep banh 1 | MPa   | No       |
| allowableBendingStressGear2Mpa | number | Ung suat uon cho phep banh 2 | MPa   | No       |

**SpurGearGeometry**

| Field              | Type         | Meaning                     | Unit  | Nullable |
| ------------------ | ------------ | --------------------------- | ----- | -------- |
| centerDistanceAwMm | number       | Khoang cach truc aw         | mm    | No       |
| moduleMSelected    | number       | Module chon                 | mm    | No       |
| teethZ1            | number (int) | So rang banh 1              | -     | No       |
| teethZ2            | number (int) | So rang banh 2              | -     | No       |
| actualRatioU3      | number       | Ty so thuc te               | ratio | No       |
| ratioErrorPercent  | number       | Sai so ty so                | %     | No       |
| diameterDw1Mm      | number       | Duong kinh vong chia banh 1 | mm    | No       |
| diameterDw2Mm      | number       | Duong kinh vong chia banh 2 | mm    | No       |
| widthBwMm          | number       | Be rong banh rang           | mm    | No       |

**DerivedFactors**

| Field        | Type   | Meaning                | Unit  | Nullable |
| ------------ | ------ | ---------------------- | ----- | -------- |
| epsilonAlpha | number | He so chong trung khop | ratio | No       |
| zEpsilon     | number | He so Z epsilon        | ratio | No       |
| yEpsilon     | number | He so Y epsilon        | ratio | No       |
| yF1          | number | He so YF1              | ratio | No       |
| yF2          | number | He so YF2              | ratio | No       |
| loadFactorKh | number | He so tai KH           | ratio | No       |
| loadFactorKf | number | He so tai KF           | ratio | No       |

**StressCheck**

| Field                  | Type    | Meaning                       | Unit    | Nullable |
| ---------------------- | ------- | ----------------------------- | ------- | -------- |
| sigmaHMpa              | number  | Ung suat tiep xuc tinh toan   | MPa     | No       |
| sigmaF1Mpa             | number  | Ung suat uon banh 1 tinh toan | MPa     | No       |
| sigmaF2Mpa             | number  | Ung suat uon banh 2 tinh toan | MPa     | No       |
| contactStressPass      | boolean | Dat ung suat tiep xuc         | boolean | No       |
| bendingStressGear1Pass | boolean | Dat ung suat uon banh 1       | boolean | No       |
| bendingStressGear2Pass | boolean | Dat ung suat uon banh 2       | boolean | No       |

**ShaftForceSummary** (phan tu trong `shaftForces`)

| Field      | Type   | Meaning                           | Unit | Nullable    |
| ---------- | ------ | --------------------------------- | ---- | ----------- |
| shaftCode  | string | Ma truc (`SHAFT_2`, `SHAFT_3`)    | -    | No          |
| shaftLabel | string | Label hien thi (vi du: "Shaft 2") | -    | No          |
| ftN        | number | Luc vong                          | N    | No          |
| frN        | number | Luc huong kinh                    | N    | No          |
| faN        | number | Luc doc truc                      | N    | No (luon 0) |

**calculationNotes**

- Danh sach ghi chu tinh toan (string), co the de trong.

### 4.3 Mapping field -> nhan UI goi y

| Field path                          | Nhan UI de xuat     | Ghi chu                |
| ----------------------------------- | ------------------- | ---------------------- |
| spurGearGeometry.centerDistanceAwMm | Khoang cach truc aw | Thong so chinh         |
| spurGearGeometry.moduleMSelected    | Module m            | Thong so chinh         |
| spurGearGeometry.teethZ1            | So rang Z1          | Thong so chinh         |
| spurGearGeometry.teethZ2            | So rang Z2          | Thong so chinh         |
| spurGearGeometry.actualRatioU3      | Ty so thuc te U3    | So sanh voi U3 yeu cau |
| spurGearGeometry.ratioErrorPercent  | Sai so ty so        | Hien thi %             |
| stressCheck.sigmaHMpa               | Sigma H             | Ung suat tiep xuc      |
| stressCheck.sigmaF1Mpa              | Sigma F1            | Ung suat uon banh 1    |
| stressCheck.sigmaF2Mpa              | Sigma F2            | Ung suat uon banh 2    |
| stressCheck.contactStressPass       | Dat tiep xuc        | Badge pass/fail        |
| stressCheck.bendingStressGear1Pass  | Dat uon banh 1      | Badge pass/fail        |
| stressCheck.bendingStressGear2Pass  | Dat uon banh 2      | Badge pass/fail        |
| shaftForces[].ftN                   | Luc vong Ft         | Theo tung truc         |
| shaftForces[].frN                   | Luc huong kinh Fr   | Theo tung truc         |
| shaftForces[].faN                   | Luc doc truc Fa     | Luon 0                 |

### 4.4 Mapping field -> don vi

| Field path                                  | Don vi  |
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

### 4.5 Truong nen hien thi o summary card

- `centerDistanceAwMm`, `moduleMSelected`, `teethZ1`, `teethZ2`.
- `actualRatioU3`, `ratioErrorPercent`.
- `sigmaHMpa`, `sigmaF1Mpa`, `sigmaF2Mpa`.
- `contactStressPass`, `bendingStressGear1Pass`, `bendingStressGear2Pass`.

## 5. Vi du response

### 5.1 Thanh cong - Module 4 calculate

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

### 5.2 Thanh cong - Module 4 history

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

**Luu y full-flow history:** `module4Result` co the `null` neu case chua tinh Module 4; kiem tra `warnings` trong response full-flow history de hien thi thong bao hop ly.

## 6. Error contract

**Exact global error wrapper not found in inspected files; verify with backend once app is running.**

| Tinh huong                         | Exception (neu co)                                | HTTP status | Go i xu ly mobile                   |
| ---------------------------------- | ------------------------------------------------- | ----------- | ----------------------------------- |
| Thieu `designCaseId` trong request | Bean Validation                                   | 400         | Hien thi "Thieu designCaseId"       |
| `designCaseId` khong ton tai       | Module4DesignCaseNotFoundException                | 404         | Hien thi "Khong tim thay case"      |
| Thieu Module 1 result              | Module4PrerequisiteMissingException               | 422         | Hien thi "Can tinh Module 1 truoc"  |
| Thieu Shaft 2 state                | Module4PrerequisiteMissingException               | 422         | Hien thi "Thieu du lieu Shaft 2"    |
| Input khong duong (>0)             | Bean Validation hoac InvalidModule4InputException | 400 / 422   | Hien thi "Gia tri phai > 0"         |
| Module 4 history khong ton tai     | Module4ResultNotFoundException                    | 404         | Hien thi "Chua co ket qua Module 4" |

## 7. Luong tich hop mobile

**A. Tinh toan day du**

1. POST `/api/v1/drivetrain/full-flow/calculate`.
2. Doc `module4Result` trong response.
3. Luu `caseSummary.designCaseId`.

**B. Case da ton tai (tinh rieng Module 4)**

1. POST `/api/v1/module-4/calculate` chi voi `designCaseId` hoac kem override.
2. Neu can xem lich su: GET `/api/v1/module-4/history/{designCaseId}`.

**C. Man hinh chi doc**

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

## 9. Kotlin models

Neu can do chinh xac cao (tinh toan/luu tru), nen dung `BigDecimal` hoac `String`. Neu chi hien thi UI, `Double` co the chap nhan.

```kotlin
import java.math.BigDecimal

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
) {
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
				val shaftCode: String,
				val shaftLabel: String,
				val ftN: BigDecimal,
				val frN: BigDecimal,
				val faN: BigDecimal
		)
}
```

## 10. UI notes

- Thu tu goi y: InputSummary -> SpurGearGeometry -> StressCheck -> ShaftForces -> DerivedFactors -> calculationNotes.
- Lam tron hien thi: so do dai/duong kinh 2-3 chu so thap phan, ung suat 1-2 chu so thap phan, ratioErrorPercent 2 chu so.
- `contactStressPass`, `bendingStressGear1Pass`, `bendingStressGear2Pass`: hien thi badge PASS/FAIL mau sac ro rang.
- `calculationNotes`: hien thi o detail view, khong can show het o summary.
- `faN` luon 0 (banh rang tru thang), co the an hoac ghi chu ngan.
- `shaftForces` duoc sap xep theo Shaft 2 -> Shaft 3, co the dung thu tu nay tren UI.

## 11. QA checklist cho mobile

- Tinh toan Module 4 chi voi `designCaseId` (khong override).
- Tinh toan Module 4 voi day du override input.
- Mo history Module 4 theo `designCaseId`.
- Xu ly truong hop thieu Module 1 prerequisite.
- Xu ly truong hop cac boolean pass/fail = false.
- Xu ly full-flow history co `module4Result` null va `warnings`.
