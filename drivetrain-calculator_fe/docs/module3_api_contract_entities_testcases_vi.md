# Module 3 - Tài liệu API Contract, Giải thích Entity và Test Case

## Mục đích

Tài liệu này mô tả **contract backend hiện tại** của **Module 3** trong project Spring Boot này.

Module 3 là module tính toán **bộ truyền bánh răng côn răng thẳng**.
Module này:
- sử dụng một `DesignCase` đã được lưu
- phụ thuộc vào **kết quả Module 1** đã tồn tại
- cho phép chọn vật liệu bánh răng
- tính toán hình học bộ truyền bánh răng côn
- tính ứng suất cho phép
- kiểm tra điều kiện bền tiếp xúc và bền uốn
- xuất vector lực trên trục 1 và trục 2
- lưu kết quả để có thể mở lại sau này

Tài liệu này gồm:
1. REST API contract
2. request/response
3. hành vi lỗi
4. entity và ý nghĩa các thuộc tính
5. test case gợi ý để test Module 3
6. các giả định và placeholder hiện tại

---

## Base Path

```text
/api/v1/module-3
```

---

## Luồng sử dụng tổng quát

Trong implementation hiện tại, Module 3 **không chạy độc lập hoàn toàn**.
Nó yêu cầu một `DesignCase` đã có **Module 1 result** hợp lệ.

Luồng điển hình cho frontend hoặc tester:

1. Gọi `POST /api/v1/module-1/calculate`
2. Lấy `caseInfo.designCaseId`
3. Gọi `GET /api/v1/module-3/materials`
4. Chọn `materialId`
5. Gọi `POST /api/v1/module-3/calculate`
6. Nếu cần mở lại thì gọi `GET /api/v1/module-3/history/{designCaseId}`

---

# 1. API Endpoints

## 1.1 Lấy danh sách vật liệu bánh răng

- **Method:** `GET`
- **Path:** `/api/v1/module-3/materials`

### Response thành công
- **Status:** `200 OK`

### Ví dụ response

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

### Ý nghĩa các field

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `materialId` | `number` | Khóa chính của vật liệu |
| `materialCode` | `string` | Mã vật liệu ổn định |
| `materialName` | `string` | Tên hiển thị |
| `heatTreatment` | `string \| null` | Mô tả nhiệt luyện/xử lý nhiệt |
| `hbMin` | `number` | Độ cứng Brinell nhỏ nhất |
| `hbMax` | `number` | Độ cứng Brinell lớn nhất |
| `sigmaBMpa` | `number` | Thông số bền kéo dùng trong Module 3 |
| `sigmaChMpa` | `number` | Thông số giới hạn chảy dùng trong Module 3 |

---

## 1.2 Tính toán kết quả Module 3

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

### Định nghĩa field request

| Field | Type | Bắt buộc | Ý nghĩa |
| --- | --- | --- | --- |
| `designCaseId` | `number` | Có | ID của bài tính đã lưu. Phải tồn tại trong hệ thống. |
| `inputT1Nmm` | `number` | Không | Moment xoắn đầu vào trên trục 1, đơn vị N.mm. Nếu bỏ trống, backend lấy từ `Module 1 -> SHAFT_1.torqueNmm`. |
| `inputN1Rpm` | `number` | Không | Số vòng quay đầu vào trên trục 1. Nếu bỏ trống, backend lấy từ `Module 1 -> SHAFT_1.rpm`. |
| `inputU2` | `number` | Không | Tỷ số truyền bánh răng côn yêu cầu. Nếu bỏ trống, backend lấy từ `Module 1 -> bevelGearRatioU2`. |
| `serviceLifeHours` | `number` | Không | Thời gian phục vụ `Lh`. Nếu bỏ trống, backend dùng `DesignCase.serviceLifeHours`; nếu vẫn chưa có thì dùng mặc định `43200`. |
| `materialId` | `number` | Có | ID vật liệu được chọn từ endpoint `/materials`. |

### Quy tắc resolve input hiện tại

#### Torque `inputT1Nmm`
1. Nếu request có truyền thì dùng request
2. Nếu không có thì lấy từ `Module 1` ở shaft state có `shaftCode = SHAFT_1`

#### Speed `inputN1Rpm`
1. Nếu request có truyền thì dùng request
2. Nếu không có thì lấy từ `Module 1` ở shaft state có `shaftCode = SHAFT_1`

#### Ratio `inputU2`
1. Nếu request có truyền thì dùng request
2. Nếu không có thì lấy từ field `bevelGearRatioU2` của `Module1Result`

#### Service life `serviceLifeHours`
1. Nếu request có truyền thì dùng request
2. Nếu không có thì lấy từ `DesignCase.serviceLifeHours`
3. Nếu vẫn không có thì backend dùng giá trị mặc định `43200`

---

### Response thành công

- **Status:** `200 OK`

### Ví dụ response

> Ghi chú:
> - ID và timestamp phụ thuộc dữ liệu thật trong DB.
> - Bộ số dưới đây tương ứng với implementation hiện tại cho input kiểu reference:
>   `T1 = 83851.991`, `n1 = 812`, `U2 = 3.14`, `Lh = 43200`, vật liệu `C40XH_QT`.

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

### Giải thích các block trong response

#### `resultInfo`

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `resultId` | `number` | Khóa chính của bản ghi `Module3Result` |
| `createdAt` | `string` | Timestamp tạo bản ghi, định dạng ISO-8601 |
| `updatedAt` | `string` | Timestamp cập nhật gần nhất |

#### `caseInfo`

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `designCaseId` | `number` | ID gốc của bài tính |
| `caseCode` | `string` | Mã bài tính |
| `caseName` | `string` | Tên hiển thị của bài tính |
| `status` | `string` | Trạng thái hiện tại. Khi tính thành công là `MODULE3_COMPLETED` |

#### `inputSummary`

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `inputT1Nmm` | `number` | Moment xoắn thực sự được dùng để tính |
| `inputN1Rpm` | `number` | Số vòng quay thực sự được dùng |
| `inputU2` | `number` | Tỷ số truyền U2 thực sự dùng |
| `serviceLifeHours` | `number` | Tuổi thọ phục vụ thực sự dùng |

#### `selectedMaterial`

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `materialId` | `number` | ID vật liệu |
| `materialCode` | `string` | Mã vật liệu |
| `materialName` | `string` | Tên hiển thị |
| `heatTreatment` | `string \| null` | Mô tả nhiệt luyện |
| `hbMin` | `number` | Độ cứng thấp nhất |
| `hbMax` | `number` | Độ cứng cao nhất |
| `sigmaBMpa` | `number` | Dữ liệu bền kéo đầu vào dùng bởi backend |
| `sigmaChMpa` | `number` | Dữ liệu giới hạn chảy đầu vào dùng bởi backend |

#### `allowableStresses`

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `allowableContactStressMpa` | `number` | Ứng suất tiếp xúc cho phép dùng để kiểm nghiệm |
| `allowableBendingStressMpa` | `number` | Ứng suất uốn cho phép dùng để kiểm nghiệm |

#### `gearGeometry`

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `reCalculated` | `number` | Chiều dài côn ngoài tính được `Re` |
| `de1Calculated` | `number` | Đường kính chia ngoài của bánh 1 |
| `moduleMteSelected` | `number` | Module tiêu chuẩn được chọn |
| `teethZ1` | `number` | Số răng bánh chủ động |
| `teethZ2` | `number` | Số răng bánh bị động |
| `actualRatioU2` | `number` | Tỷ số truyền thực tế sau khi làm tròn số răng |
| `widthBMm` | `number` | Chiều rộng vành răng `b` |
| `diameterDm1Mm` | `number` | Đường kính trung bình bánh 1 |
| `diameterDm2Mm` | `number` | Đường kính trung bình bánh 2 |
| `coneAngleDelta1Deg` | `number` | Góc côn của bánh 1 |
| `coneAngleDelta2Deg` | `number` | Góc côn của bánh 2 |

#### `stressCheck`

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `sigmaHMpa` | `number` | Ứng suất tiếp xúc tính được |
| `sigmaF1Mpa` | `number` | Ứng suất uốn của bánh 1 |
| `sigmaF2Mpa` | `number` | Ứng suất uốn của bánh 2 |
| `contactStressPass` | `boolean` | `true` nếu `sigmaH <= allowableContactStress` |
| `bendingStressPass` | `boolean` | `true` nếu kiểm nghiệm uốn của cả 2 bánh đều đạt |

#### `shaftForces[]`

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `shaftCode` | `string` | Hiện tại Module 3 dùng `SHAFT_1` và `SHAFT_2` |
| `shaftLabel` | `string` | Nhãn hiển thị cho frontend |
| `ftN` | `number` | Lực vòng |
| `frN` | `number` | Lực hướng tâm |
| `faN` | `number` | Lực dọc trục |

#### `calculationNotes[]`

Danh sách note do backend sinh ra để giải thích:
- input nào được kế thừa từ Module 1
- input nào dùng default
- chỗ nào còn giả định / placeholder
- thông tin sai số tỷ số truyền và trạng thái kiểm nghiệm

---

## 1.3 Mở lại kết quả Module 3 đã lưu

- **Method:** `GET`
- **Path:** `/api/v1/module-3/history/{designCaseId}`

### Path parameter

| Name | Type | Required | Ý nghĩa |
| --- | --- | --- | --- |
| `designCaseId` | `number` | Có | ID bài tính đã lưu |

### Response thành công
- **Status:** `200 OK`
- **Response body:** cùng shape với `POST /api/v1/module-3/calculate`

---

# 2. Error Behavior

## Các status code thường gặp

| Status | Khi nào |
| --- | --- |
| `200` | Xử lý thành công |
| `400` | Lỗi validation ở tầng binding request của Spring |
| `404` | Không tìm thấy `designCaseId`, `materialId`, hoặc không có Module 3 result đã lưu |
| `422` | Lỗi nghiệp vụ hoặc thiếu điều kiện tiên quyết từ Module 1 |

## Mapping exception hiện tại

| Exception | Status | Ý nghĩa |
| --- | --- | --- |
| `Module3DesignCaseNotFoundException` | `404` | Không tồn tại design case |
| `GearMaterialNotFoundException` | `404` | Không tồn tại vật liệu |
| `Module3ResultNotFoundException` | `404` | Không tìm thấy kết quả Module 3 đã lưu cho case đó |
| `Module3PrerequisiteMissingException` | `422` | Thiếu Module 1 result hoặc thiếu dữ liệu upstream cần thiết |
| `InvalidModule3InputException` | `422` | Input kỹ thuật không hợp lệ ở tầng service |

### Ví dụ response 404

```json
{
  "timestamp": "2026-04-19T10:40:00.000Z",
  "status": 404,
  "error": "Not Found",
  "path": "/api/v1/module-3/history/999999"
}
```

### Ví dụ response 422

```json
{
  "timestamp": "2026-04-19T10:40:00.000Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "path": "/api/v1/module-3/calculate"
}
```

---

# 3. Mô hình dữ liệu và giải thích Entity

## Tổng quan quan hệ

```text
DesignCase    1 --- 1 Module1Result
DesignCase    1 --- 1 Module3Result
GearMaterial  1 --- N Module3Result
Module3Result 1 --- N Module3ShaftForce
```

### Ý nghĩa nghiệp vụ

- `DesignCase` vẫn là bản ghi gốc của toàn bộ bài tính
- `Module3Result` là kết quả chính của phần bộ truyền bánh răng côn cho case đó
- `GearMaterial` là bảng danh mục dùng chung
- `Module3ShaftForce` là nơi lưu vector lực để các module tính trục về sau có thể tái sử dụng

---

## 3.1 `GearMaterial`

### Vai trò
Lưu dữ liệu vật liệu dùng lại cho việc tính bánh răng côn.

### Tên bảng hiện tại
```text
gear_material
```

### Thuộc tính

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `id` | `Long` | Khóa chính |
| `materialCode` | `String` | Mã vật liệu, ví dụ `C40XH_QT` |
| `materialName` | `String` | Tên hiển thị |
| `heatTreatment` | `String` | Mô tả xử lý nhiệt |
| `hbMin` | `BigDecimal` | Độ cứng nhỏ nhất |
| `hbMax` | `BigDecimal` | Độ cứng lớn nhất |
| `sigmaBMpa` | `BigDecimal` | Dữ liệu bền kéo dùng để suy ra ứng suất cho phép |
| `sigmaChMpa` | `BigDecimal` | Dữ liệu giới hạn chảy dùng để suy ra ứng suất cho phép |
| `createdAt` | `Instant` | Thời điểm tạo |
| `updatedAt` | `Instant` | Thời điểm cập nhật |
| `module3Results` | `List<Module3Result>` | Danh sách kết quả Module 3 dùng vật liệu này |

### Vì sao cần entity này
Nếu không có `GearMaterial`, frontend sẽ phải gửi các thông số cơ tính thô mỗi lần tính.
Lưu vào bảng giúp backend kiểm soát dữ liệu tốt hơn, trace được kết quả, và thuận tiện cho UI chọn vật liệu.

---

## 3.2 `Module3Result`

### Vai trò
Lưu kết quả chính của Module 3 cho một `DesignCase`.

### Tên bảng hiện tại
```text
module3_result
```

### Thuộc tính

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `id` | `Long` | Khóa chính |
| `inputT1Nmm` | `BigDecimal` | Moment xoắn thực tế dùng để tính |
| `inputN1Rpm` | `BigDecimal` | Số vòng quay thực tế dùng để tính |
| `inputU2` | `BigDecimal` | Tỷ số truyền U2 thực tế dùng để tính |
| `serviceLifeHours` | `BigDecimal` | Tuổi thọ phục vụ thực tế dùng |
| `allowableContactStressMpa` | `BigDecimal` | Ứng suất tiếp xúc cho phép |
| `allowableBendingStressMpa` | `BigDecimal` | Ứng suất uốn cho phép |
| `reCalculated` | `BigDecimal` | Chiều dài côn ngoài tính được |
| `de1Calculated` | `BigDecimal` | Đường kính chia ngoài của bánh 1 |
| `moduleMteSelected` | `BigDecimal` | Module tiêu chuẩn được chọn |
| `teethZ1` | `Integer` | Số răng bánh chủ động |
| `teethZ2` | `Integer` | Số răng bánh bị động |
| `actualRatioU2` | `BigDecimal` | Tỷ số truyền thực tế sau khi chốt số răng |
| `widthBMm` | `BigDecimal` | Chiều rộng vành răng |
| `diameterDm1Mm` | `BigDecimal` | Đường kính trung bình bánh 1 |
| `diameterDm2Mm` | `BigDecimal` | Đường kính trung bình bánh 2 |
| `coneAngleDelta1Deg` | `BigDecimal` | Góc côn bánh 1 |
| `coneAngleDelta2Deg` | `BigDecimal` | Góc côn bánh 2 |
| `sigmaHMpa` | `BigDecimal` | Ứng suất tiếp xúc tính được |
| `sigmaF1Mpa` | `BigDecimal` | Ứng suất uốn bánh 1 |
| `sigmaF2Mpa` | `BigDecimal` | Ứng suất uốn bánh 2 |
| `contactStressPass` | `boolean` | Cờ đạt/không đạt bền tiếp xúc |
| `bendingStressPass` | `boolean` | Cờ đạt/không đạt bền uốn |
| `calculationNote` | `String` | Ghi chú, cảnh báo, giả định được serialize thành text |
| `createdAt` | `Instant` | Thời điểm tạo |
| `updatedAt` | `Instant` | Thời điểm cập nhật |
| `designCase` | `DesignCase` | Bài tính gốc sở hữu kết quả này |
| `material` | `GearMaterial` | Vật liệu được dùng |
| `shaftForces` | `List<Module3ShaftForce>` | Các vector lực con của kết quả này |

### Vì sao cần entity này
Đây là nguồn dữ liệu chính cho màn hình kết quả Module 3.
Nó gom toàn bộ output quan trọng vào một chỗ để:
- render frontend
- mở lại kết quả đã lưu
- dùng tiếp cho các module sau

---

## 3.3 `Module3ShaftForce`

### Vai trò
Lưu vector lực trên từng trục liên quan do Module 3 sinh ra.

### Tên bảng hiện tại
```text
module3_shaft_force
```

### Thuộc tính

| Field | Type | Ý nghĩa |
| --- | --- | --- |
| `id` | `Long` | Khóa chính |
| `shaftCode` | `ShaftCode` | Hiện tại Module 3 dùng `SHAFT_1` và `SHAFT_2` |
| `ftN` | `BigDecimal` | Lực vòng |
| `frN` | `BigDecimal` | Lực hướng tâm |
| `faN` | `BigDecimal` | Lực dọc trục |
| `createdAt` | `Instant` | Thời điểm tạo |
| `module3Result` | `Module3Result` | Kết quả Module 3 cha |

### Ràng buộc unique
Có unique constraint trên:
- `module3_result_id`
- `shaft_code`

Điều này đảm bảo với mỗi kết quả Module 3 chỉ có tối đa một vector lực cho mỗi trục.

---

## 3.4 Các field của `DesignCase` liên quan tới Module 3

Mặc dù `DesignCase` là entity chung cho toàn bộ flow, các field sau ảnh hưởng trực tiếp tới Module 3:

| Field | Ý nghĩa với Module 3 |
| --- | --- |
| `id` | ID gốc dùng để gọi API |
| `caseCode` | Mã nghiệp vụ của bài tính |
| `caseName` | Tên hiển thị |
| `serviceLifeHours` | Giá trị fallback của `Lh` nếu request Module 3 không truyền |
| `status` | Sau khi tính Module 3 thành công sẽ thành `MODULE3_COMPLETED` |
| `module1Result` | Điều kiện tiên quyết bắt buộc |
| `module3Result` | Kết quả Module 3 hiện tại của case |

---

# 4. Ghi chú về logic và placeholder hiện tại

Implementation hiện tại của Module 3 được làm theo hướng **dùng được, nhất quán, dễ tích hợp**, nhưng chưa phải phiên bản cơ khí chuẩn cuối cùng.

## Các giả định hiện đang áp dụng

1. Module 3 phụ thuộc vào `Module1Result` đã lưu
2. Nếu request không truyền `T1`, `n1`, hoặc `U2` thì backend kế thừa từ Module 1
3. Nếu không truyền `serviceLifeHours` và `DesignCase` cũng chưa có, backend dùng mặc định `43200`
4. Vật liệu lấy từ danh mục `GearMaterial` đã seed
5. Việc chọn số răng dùng module tiêu chuẩn + số răng nguyên, sau đó ghi nhận sai số tỷ số truyền thực tế
6. Vector lực được xuất cho `SHAFT_1` và `SHAFT_2`

## Các placeholder / đơn giản hóa hiện tại

1. **Mô hình ứng suất cho phép đang là bản đơn giản hóa**
   - hệ số tuổi thọ, công thức suy ra ứng suất cho phép, và một số hệ số đang ở mức xấp xỉ thực dụng
   - chưa phải full bảng tra / giáo trình chuẩn cuối cùng

2. **Hệ số dạng răng đang xấp xỉ**
   - chưa dùng bảng tra hoàn chỉnh

3. **`U2` kế thừa từ Module 1 có thể vẫn là placeholder**
   - vì hiện tại Module 1 vẫn đang lưu `U2 = 3.14` làm tỷ số truyền tạm nếu người dùng không override

Những điểm này được đưa rõ vào `calculationNotes` để frontend có thể hiển thị.

---

# 5. Test Case gợi ý cho Module 3

## Điều kiện trước khi test

Trước khi test Module 3, nên tạo một case qua Module 1 trước.

### Bước A - tạo seed case bằng Module 1

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

**Kỳ vọng chính**
- HTTP `200`
- response có `caseInfo.designCaseId`
- response có `transmissionRatios.bevelRatioU2`
- response có shaft state `SHAFT_1`

Dùng `designCaseId` đó cho các test case Module 3 bên dưới.

---

## TC-01 - lấy danh sách vật liệu

### Mục đích
Kiểm tra dữ liệu reference vật liệu có sẵn.

### Request
```http
GET /api/v1/module-3/materials
```

### Kỳ vọng
- status `200`
- response là một JSON array không rỗng
- mỗi object có:
  - `materialId`
  - `materialCode`
  - `hbMin`
  - `hbMax`
  - `sigmaBMpa`
  - `sigmaChMpa`

---

## TC-02 - tính Module 3 bằng dữ liệu kế thừa từ Module 1

### Mục đích
Kiểm tra happy path khi chỉ truyền `designCaseId` và `materialId`.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 8,
  "materialId": 1
}
```

### Kỳ vọng
- status `200`
- `caseInfo.status = MODULE3_COMPLETED`
- `inputSummary.inputT1Nmm` lấy từ torque của `SHAFT_1` trong Module 1
- `inputSummary.inputN1Rpm` lấy từ rpm của `SHAFT_1` trong Module 1
- `inputSummary.inputU2` lấy từ Module 1 result
- `inputSummary.serviceLifeHours = 43200.000000` nếu case trước đó chưa có `serviceLifeHours`
- `selectedMaterial.materialId = 1`
- `gearGeometry.teethZ1` và `gearGeometry.teethZ2` là số nguyên dương
- `shaftForces` có đúng 2 phần tử:
  - `SHAFT_1`
  - `SHAFT_2`
- `stressCheck.contactStressPass` là boolean
- `stressCheck.bendingStressPass` là boolean
- `calculationNotes` giải thích rõ việc kế thừa/default

---

## TC-03 - tính Module 3 với dữ liệu override trực tiếp

### Mục đích
Xác nhận request override được ưu tiên hơn dữ liệu Module 1.

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

### Kỳ vọng
- status `200`
- `inputSummary` khớp với giá trị request truyền vào
- `calculationNotes` nói rõ torque/speed/U2 được override từ request
- kết quả được persist và có thể mở lại

---

## TC-04 - mở lại kết quả Module 3 đã lưu

### Mục đích
Kiểm tra persistence và chức năng reopen.

### Request
```http
GET /api/v1/module-3/history/8
```

### Kỳ vọng
- status `200`
- response shape giống calculate response
- `resultInfo.resultId` tồn tại
- `caseInfo.designCaseId = 8`
- có geometry, stress, shaft forces

---

## TC-05 - tính lại cùng một case phải thay kết quả Module 3 cũ

### Mục đích
Xác nhận khi recalculation thì bản ghi cũ bị thay thế.

### Các bước
1. Gọi `POST /calculate` với `materialId = 1`
2. Gọi `POST /calculate` lần nữa với cùng `designCaseId` nhưng `materialId = 2`
3. Gọi `GET /history/{designCaseId}`

### Kỳ vọng
- cả hai lần gọi đều `200`
- kết quả mở lại phản ánh request lần hai
- case chỉ còn một `Module3Result` hiện hành
- `selectedMaterial` trong response cuối là vật liệu `2`

---

## TC-06 - design case ID không tồn tại

### Mục đích
Kiểm tra xử lý not found.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 999999,
  "materialId": 1
}
```

### Kỳ vọng
- status `404`
- message/backend error cho biết không tìm thấy design case

---

## TC-07 - material ID không tồn tại

### Mục đích
Kiểm tra validate lookup vật liệu.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 8,
  "materialId": 999999
}
```

### Kỳ vọng
- status `404`
- backend báo không tìm thấy gear material

---

## TC-08 - gọi Module 3 cho case chưa có Module 1 result

### Mục đích
Kiểm tra điều kiện tiên quyết của Module 1.

### Setup
Tạo một `DesignCase` trong DB nhưng chưa chạy Module 1, hoặc dùng một case đã biết là chưa có `Module1Result`.

### Request
```http
POST /api/v1/module-3/calculate
Content-Type: application/json

{
  "designCaseId": 15,
  "materialId": 1
}
```

### Kỳ vọng
- status `422`
- lỗi cho biết cần có Module 1 result trước khi tính Module 3

---

## TC-09 - override input âm / không hợp lệ

### Mục đích
Kiểm tra validate số học.

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

### Kỳ vọng
- status `400` hoặc `422` tùy lỗi bị chặn ở tầng bean validation hay service
- request không được thành công

---

## TC-10 - xác nhận cơ chế reuse `serviceLifeHours`

### Mục đích
Kiểm tra fallback `serviceLifeHours` từ `DesignCase`.

### Các bước
1. Gọi Module 3 với:
   ```json
   {
     "designCaseId": 8,
     "serviceLifeHours": 50000,
     "materialId": 1
   }
   ```
2. Gọi Module 3 lần nữa cho cùng case nhưng không truyền `serviceLifeHours`

### Kỳ vọng
- response lần hai dùng `inputSummary.serviceLifeHours = 50000.000000`
- notes ghi rõ service life được reuse từ saved design case

---

# 6. Script test thủ công gợi ý

## 6.1 Tạo seed case bằng Module 1

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

## 6.2 Lấy danh sách vật liệu

```bash
curl http://localhost:8080/api/v1/module-3/materials
```

## 6.3 Tính Module 3 bằng dữ liệu kế thừa từ upstream

```bash
curl -X POST http://localhost:8080/api/v1/module-3/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "designCaseId": 8,
    "materialId": 1
  }'
```

## 6.4 Mở lại kết quả đã lưu

```bash
curl http://localhost:8080/api/v1/module-3/history/8
```

---

# 7. Ghi chú tích hợp frontend

- Luôn lấy `designCaseId` hợp lệ từ Module 1 trước
- Dùng `/materials` để populate dropdown chọn vật liệu
- Response calculate đã đủ tốt cho màn hình kết quả:
  - input summary
  - selected material
  - allowable stress
  - gear geometry
  - validation flags
  - shaft forces
  - notes/warnings
- Nên hiển thị `calculationNotes` trên UI vì đây là nơi backend ghi rõ giá trị nào kế thừa và placeholder nào còn đang dùng
- Nếu người dùng tính lại Module 1 cho cùng case, Module 3 cũ sẽ bị invalidated ở backend và cần tính lại

---

# 8. Các vật liệu seed hiện tại

Các vật liệu sau được `DataInitializer` tạo nếu chưa tồn tại:

| materialCode | materialName | heatTreatment | hbMin | hbMax | sigmaBMpa | sigmaChMpa |
| --- | --- | --- | ---: | ---: | ---: | ---: |
| `C40XH_QT` | Steel C40XH | Quenched and tempered | 235 | 262 | 850 | 650 |
| `40CR_N` | Steel 40Cr | Normalized | 207 | 241 | 780 | 540 |
| `C45_N` | Steel C45 | Normalized | 179 | 207 | 600 | 355 |

---

# 9. Tóm tắt

Module 3 hiện tại đã hỗ trợ:
- tra cứu vật liệu
- tính toán bevel gear theo flow saved-case
- kế thừa dữ liệu upstream từ Module 1
- cho phép override input kỹ thuật
- lưu và mở lại kết quả
- output hình học
- cờ kiểm nghiệm bền
- vector lực trên trục
- note giải thích giả định và placeholder

Implementation hiện tại đã đủ để tích hợp và test trong phase sản phẩm hiện nay, đồng thời vẫn đánh dấu rõ các phần cơ khí còn ở mức xấp xỉ/chưa chốt cuối.
