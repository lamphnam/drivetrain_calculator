# Bao cao Design Patterns - Du an Drivetrain Calculator

> Mon hoc: Cong nghe phan mem (CNPM) - HK252
> Nhom: [Ten nhom]
> Ngay: 2026-05-14

---

## 1. Tong quan yeu cau CNPM

Du an Drivetrain Calculator la backend REST API tinh toan he thong truyen dong (dong co -> dai -> banh rang con -> banh rang tru -> truc thung tron). Theo huong CNPM, nhom can chung minh viec ap dung design patterns mot cach co y thuc va hop ly trong kien truc phan mem.

Bao cao nay trinh bay 3 design patterns chinh duoc su dung trong du an, voi bang chung cu the tu source code.

---

## 2. Danh sach design patterns su dung

| # | Pattern | Phan loai | Muc dich chinh |
|---|---------|-----------|----------------|
| 1 | Facade Pattern | GoF - Structural | An di su phuc tap cua viec dieu phoi 3 module tinh toan |
| 2 | Strategy Pattern (Priority Cascade) | GoF - Behavioral | Giai quyet gia tri ung suat cho phep theo thu tu uu tien |
| 3 | Builder Pattern | GoF - Creational | Xay dung entity phuc tap co 20+ truong mot cach ro rang |

---

## 3. Pattern 1: Facade Pattern

### Ten pattern
Facade (Mat tien)

### Phan loai
GoF - Structural Pattern

### Muc dich trong du an
`FullFlowCalculationService` dong vai tro Facade, an di toan bo quy trinh dieu phoi Module 1 -> Module 3 -> Module 4 phia sau mot method duy nhat. Controller chi can goi mot service, khong can biet thu tu xu ly hay cach cac module phu thuoc nhau.

### Cac class/file lien quan

| File | Vai tro |
|------|---------|
| `src/main/java/com/drivetrain/fullflow/service/FullFlowCalculationService.java` | **Facade** - dieu phoi 3 module services |
| `src/main/java/com/drivetrain/fullflow/controller/FullFlowCalculationController.java` | Client cua Facade |
| `src/main/java/com/drivetrain/module1/service/Module1CalculationService.java` | Subsystem 1 |
| `src/main/java/com/drivetrain/module3/service/Module3CalculationService.java` | Subsystem 2 |
| `src/main/java/com/drivetrain/module4/service/Module4CalculationService.java` | Subsystem 3 |

### Bang chung code

Controller chi co 1 dependency duy nhat:

```java
@RestController
@RequestMapping("/api/v1/drivetrain/full-flow")
@RequiredArgsConstructor
public class FullFlowCalculationController {
    private final FullFlowCalculationService fullFlowCalculationService;

    @PostMapping("/calculate")
    public FullFlowCalculationResponse calculate(@Valid @RequestBody FullFlowCalculationRequest request) {
        return fullFlowCalculationService.calculate(request);
    }
}
```

Facade an di toan bo logic dieu phoi:

```java
@Service
@RequiredArgsConstructor
public class FullFlowCalculationService {
    private final Module1CalculationService module1CalculationService;
    private final Module3CalculationService module3CalculationService;
    private final Module4CalculationService module4CalculationService;
    // ...

    @Transactional
    public FullFlowCalculationResponse calculate(FullFlowCalculationRequest request) {
        // Step 1 - Module 1: chon dong co, tinh ty so truyen
        Module1CalculationResponse module1Response = module1CalculationService.calculate(module1Request);

        // Step 2 - Module 3: tinh banh rang con
        Module3CalculationResponse module3Response = module3CalculationService.calculate(module3Request);

        // Step 3 - Module 4: tinh banh rang tru
        Module4CalculationResponse module4Response = module4CalculationService.calculate(module4Request);

        return new FullFlowCalculationResponse(caseSummary, module1Response, module3Response, module4Response, ...);
    }
}
```

### Tai sao day la vi du hop le

- Controller khong biet Module 1 phai chay truoc Module 3, hay Module 4 can ket qua tu Module 1.
- Neu thu tu tinh toan thay doi (vi du them Module 2 trong tuong lai), chi can sua Facade, khong anh huong controller hay mobile client.
- Client gui 1 request, nhan 1 response tong hop - khong can goi 3 API rieng le.

### Loi ich

- **Separation of Concerns:** Controller chi lo HTTP, Facade lo orchestration.
- **Extensibility:** Them module moi chi can sua Facade.
- **Testability:** Co the test Facade doc lap bang cach mock 3 module services.

---

## 4. Pattern 2: Strategy Pattern (Priority-based Value Resolution)

### Ten pattern
Strategy (Chien luoc) - bien the Priority Cascade

### Phan loai
GoF - Behavioral Pattern

### Muc dich trong du an
Module 4 can gia tri ung suat cho phep (allowable stress) de kiem nghiem banh rang. Gia tri nay co the den tu 3 nguon khac nhau theo thu tu uu tien:
1. **REQUEST** - nguoi dung truyen truc tiep trong request
2. **MODULE3** - ke thua tu ket qua Module 3 da tinh truoc do
3. **DEFAULT** - gia tri mac dinh cua he thong (600/260/260 MPa)

Moi nguon la mot "strategy" giai quyet gia tri, va he thong chon strategy dau tien kha dung.

### Cac class/file lien quan

| File | Vai tro |
|------|---------|
| `src/main/java/com/drivetrain/module4/service/Module4CalculationService.java` | Chua logic resolution |
| Enum `ValueSource` (dong 374-379) | Dinh danh strategy nao duoc chon |
| Record `StressResolution` (dong 395-401) | Ket qua cua strategy duoc chon |
| Record `ResolvedInputs` (dong 381-393) | Tong hop tat ca inputs da resolve + source tracking |

### Bang chung code

Enum danh dau nguon goc gia tri:

```java
private enum ValueSource {
    REQUEST,   // Strategy 1: nguoi dung cung cap
    MODULE1,   // Strategy 2: ke thua tu Module 1
    MODULE3,   // Strategy 3: ke thua tu Module 3
    DEFAULT    // Strategy 4: gia tri mac dinh he thong
}
```

Method `resolveStresses()` thuc hien priority cascade:

```java
private StressResolution resolveStresses(Module4CalculationRequest request, Long designCaseId) {
    // Strategy 1: Request cung cap -> uu tien cao nhat
    if (request.allowableContactStressMpa() != null
            || request.allowableBendingStressGear1Mpa() != null
            || request.allowableBendingStressGear2Mpa() != null) {
        return new StressResolution(
                scale(request.allowableContactStressMpa() != null
                        ? request.allowableContactStressMpa() : DEFAULT_ALLOWABLE_CONTACT_STRESS),
                // ...
                ValueSource.REQUEST
        );
    }

    // Strategy 2: Ke thua tu Module 3 -> uu tien trung binh
    return module3ResultRepository.findByDesignCaseId(designCaseId)
            .map(module3Result -> new StressResolution(
                    scale(module3Result.getAllowableContactStressMpa() != null
                            ? module3Result.getAllowableContactStressMpa() : DEFAULT_ALLOWABLE_CONTACT_STRESS),
                    // ...
                    ValueSource.MODULE3
            ))
            // Strategy 3: Default -> uu tien thap nhat
            .orElse(new StressResolution(
                    scale(DEFAULT_ALLOWABLE_CONTACT_STRESS),
                    scale(DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR1),
                    scale(DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR2),
                    ValueSource.DEFAULT
            ));
}
```

Source tracking duoc su dung de sinh calculation notes:

```java
switch (inputs.stressSource()) {
    case REQUEST -> notes.add("Allowable stresses were provided directly in the Module 4 request payload.");
    case MODULE3 -> notes.add("Allowable stresses were inherited from the Module 3 result...");
    case DEFAULT -> notes.add("Allowable stresses used backend defaults...");
}
```

### Tai sao day la vi du hop le

- Co ro rang 3 chien luoc giai quyet gia tri voi thu tu uu tien xac dinh.
- `ValueSource` enum lam cho viec chon strategy co the trace duoc (audit trail).
- Ket qua `StressResolution` record dong goi ca gia tri lan metadata ve nguon goc.
- Logic tuong tu ap dung cho T2, n2, U3 (REQUEST vs MODULE1).

### Loi ich

- **Maintainability:** Them nguon du lieu moi (vi du: user profile defaults) chi can them 1 nhanh trong cascade.
- **Traceability:** `calculationNotes` trong response cho nguoi dung biet gia tri den tu dau.
- **Testability:** Co the test tung truong hop (request co/khong co gia tri, Module 3 ton tai/khong ton tai).

---

## 5. Pattern 3: Builder Pattern

### Ten pattern
Builder (Xay dung)

### Phan loai
GoF - Creational Pattern

### Muc dich trong du an
Cac entity nhu `Module4Result` co 25+ truong. Builder pattern cho phep xay dung object theo tung buoc, ro rang, khong can constructor khong lo.

### Cac class/file lien quan

| File | Vai tro |
|------|---------|
| `src/main/java/com/drivetrain/domain/entity/Module4Result.java` | Entity voi @Builder (25+ fields) |
| `src/main/java/com/drivetrain/domain/entity/Module1Result.java` | Entity voi @Builder (12+ fields) |
| `src/main/java/com/drivetrain/domain/entity/Module3Result.java` | Entity voi @Builder (25+ fields) |
| `src/main/java/com/drivetrain/domain/entity/Module4ShaftForce.java` | Entity voi @Builder |
| `src/main/java/com/drivetrain/module4/service/Module4CalculationService.java` | Noi su dung builder |

### Bang chung code

Entity khai bao Builder:

```java
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Module4Result {
    // 25+ fields: inputT2Nmm, inputN2Rpm, inputU3, centerDistanceAwMm,
    // moduleMSelected, teethZ1, teethZ2, actualRatioU3, ratioErrorPercent,
    // diameterDw1Mm, diameterDw2Mm, widthBwMm, epsilonAlpha, zEpsilon,
    // yEpsilon, yF1, yF2, loadFactorKh, loadFactorKf, sigmaHMpa,
    // sigmaF1Mpa, sigmaF2Mpa, contactStressPass, bendingStressGear1Pass,
    // bendingStressGear2Pass, calculationNote, ...
}
```

Su dung builder trong service (Module4CalculationService.java, dong 61-92):

```java
Module4Result module4Result = Module4Result.builder()
        .designCase(designCase)
        .inputT2Nmm(inputs.inputT2Nmm())
        .inputN2Rpm(inputs.inputN2Rpm())
        .inputU3(inputs.inputU3())
        .allowableContactStressMpa(inputs.allowableContactStressMpa())
        .allowableBendingStressGear1Mpa(inputs.allowableBendingStressGear1Mpa())
        .allowableBendingStressGear2Mpa(inputs.allowableBendingStressGear2Mpa())
        .centerDistanceAwMm(calculated.centerDistanceAwMm())
        .moduleMSelected(calculated.moduleMSelected())
        .teethZ1(calculated.teethZ1())
        .teethZ2(calculated.teethZ2())
        // ... 15+ fields nua
        .build();
```

### Tai sao day la vi du hop le

- Module4Result co 25+ truong - neu dung constructor se rat kho doc va de nham thu tu tham so.
- Builder cho phep dat ten tung field ro rang, IDE ho tro autocomplete.
- `@Builder.Default` duoc su dung cho cac field co gia tri mac dinh (vi du: `shaftForces = new ArrayList<>()`).

### Loi ich

- **Readability:** Code xay dung entity doc duoc nhu van ban, khong phai nho thu tu 25 tham so.
- **Safety:** Khong the nham lan thu tu tham so (compile-time safety qua method names).
- **Flexibility:** Co the bo qua optional fields ma khong can overload constructor.

---

## 6. Luong hoat dong minh hoa

### Request Full-Flow: POST /api/v1/drivetrain/full-flow/calculate

```
Client (Mobile/Postman)
    |
    | POST { requiredPowerKw: 5.5, requiredOutputRpm: 70 }
    v
FullFlowCalculationController          <-- Thin controller (HTTP only)
    |
    | delegate
    v
FullFlowCalculationService             <-- FACADE PATTERN
    |
    |--- (1) Module1CalculationService.calculate()
    |         |-> Module1EngineeringCalculator (pure math)
    |         |-> Module1ResultRepository.save()
    |         |<- Module1CalculationResponse
    |
    |--- (2) Module3CalculationService.calculate()
    |         |-> Module3EngineeringCalculator (pure math)
    |         |-> Module3ResultRepository.save()
    |         |<- Module3CalculationResponse
    |
    |--- (3) Module4CalculationService.calculate()
    |         |-> resolveStresses()            <-- STRATEGY PATTERN
    |         |     Priority: REQUEST > MODULE3 > DEFAULT
    |         |-> Module4EngineeringCalculator (pure math)
    |         |-> Module4Result.builder()...   <-- BUILDER PATTERN
    |         |-> Module4ResultRepository.save()
    |         |<- Module4CalculationResponse
    |
    v
FullFlowCalculationResponse (combined)
    |
    v
Client
```

---

## 7. Truoc va sau refactor

**Khong can refactor vi code hien tai da co du pattern ro rang.**

Ca 3 patterns deu da duoc implement tu dau trong qua trinh phat trien:
- Facade duoc tao khi implement full-flow endpoint.
- Strategy cascade duoc thiet ke khi Module 4 can ke thua stress tu Module 3.
- Builder duoc su dung nhat quan cho moi entity co nhieu truong.

---

## 8. Cach demo voi giang vien

### Script trinh bay (3-5 phut)

**Mo dau:**
> "Trong project, nhom em su dung 3 design patterns chinh de dam bao code co kien truc ro rang va de bao tri."

**Pattern 1 - Facade:**
> "Thu nhat la Facade Pattern. Class `FullFlowCalculationService` dong vai tro mat tien, an di viec dieu phoi 3 module tinh toan. Controller chi goi 1 method duy nhat, khong can biet Module 1 phai chay truoc Module 3 hay Module 4 phu thuoc vao ket qua Module 1. Loi ich la khi them module moi, chi can sua Facade ma khong anh huong API layer."

*Mo file: `FullFlowCalculationService.java` -> chi method `calculate()` voi 3 buoc ro rang.*

**Pattern 2 - Strategy:**
> "Thu hai la Strategy Pattern dang priority cascade. Khi tinh banh rang tru, he thong can ung suat cho phep. Gia tri nay co the den tu 3 nguon: nguoi dung truyen truc tiep, ke thua tu Module 3, hoac dung default. Method `resolveStresses()` chon nguon dau tien kha dung va ghi lai nguon goc bang enum `ValueSource` de tao audit trail trong response."

*Mo file: `Module4CalculationService.java` -> chi method `resolveStresses()` va enum `ValueSource`.*

**Pattern 3 - Builder:**
> "Thu ba la Builder Pattern. Entity `Module4Result` co hon 25 truong. Thay vi dung constructor khong lo, nhom dung Builder de xay dung object tung buoc, moi field co ten ro rang. Dieu nay giup tranh loi nham thu tu tham so va code de doc hon."

*Mo file: `Module4CalculationService.java` dong 61-92 -> chi builder chain.*

**Ket:**
> "Ca 3 patterns deu giai quyet van de cu the trong du an, khong phai ap dung cho co. Facade giai quyet orchestration, Strategy giai quyet multi-source input resolution, Builder giai quyet complex object construction."

---

## 9. Checklist bang chung

| # | Pattern | File can mo | Dong code quan trong |
|---|---------|-------------|---------------------|
| 1 | Facade | `fullflow/service/FullFlowCalculationService.java` | Method `calculate()` - 3 buoc dieu phoi |
| 1 | Facade | `fullflow/controller/FullFlowCalculationController.java` | Chi 1 dependency, 2 methods |
| 2 | Strategy | `module4/service/Module4CalculationService.java` | Method `resolveStresses()` dong 159-189 |
| 2 | Strategy | `module4/service/Module4CalculationService.java` | Enum `ValueSource` dong 374-379 |
| 2 | Strategy | `module4/service/Module4CalculationService.java` | Record `StressResolution` dong 395-401 |
| 2 | Strategy | `module4/service/Module4CalculationService.java` | Switch `inputs.stressSource()` dong 229-234 |
| 3 | Builder | `domain/entity/Module4Result.java` | Annotation `@Builder` |
| 3 | Builder | `module4/service/Module4CalculationService.java` | Builder chain dong 61-92 |

---

## 10. Ket luan

Du an Drivetrain Calculator ap dung 3 design patterns mot cach co y thuc va hop ly:

1. **Facade Pattern** giai quyet bai toan dieu phoi nhieu module tinh toan phuc tap, giup API layer don gian va de mo rong.

2. **Strategy Pattern (Priority Cascade)** giai quyet bai toan multi-source input resolution voi traceability, giup he thong linh hoat trong viec xac dinh gia tri dau vao tu nhieu nguon.

3. **Builder Pattern** giai quyet bai toan xay dung entity phuc tap co nhieu truong, giup code an toan va de doc.

Moi pattern deu co bang chung cu the trong source code, giai quyet van de thuc te cua du an, va co the demo truc tiep cho giang vien.
