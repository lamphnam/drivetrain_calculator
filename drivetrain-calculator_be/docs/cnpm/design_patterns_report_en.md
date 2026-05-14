# Design Patterns Report - Drivetrain Calculator Project

> Course: Software Engineering (CNPM) - HK252
> Team: [Team Name]
> Date: 2026-05-14

---

## 1. Overview

The Drivetrain Calculator is a backend REST API that computes a mechanical drivetrain system (Electric Motor -> V-belt -> Bevel Gear -> Spur Gear -> Drum Shaft). As part of the Software Engineering course requirement, the project demonstrates intentional and justified use of design patterns in its architecture.

This report presents 3 design patterns used in the project, with concrete evidence from source code.

---

## 2. Design Patterns Summary

| # | Pattern | Category | Purpose |
|---|---------|----------|---------|
| 1 | Facade Pattern | GoF - Structural | Hides the complexity of orchestrating 3 calculation modules |
| 2 | Strategy Pattern (Priority Cascade) | GoF - Behavioral | Resolves allowable stress values from multiple sources by priority |
| 3 | Builder Pattern | GoF - Creational | Constructs complex entities with 25+ fields in a readable way |

---

## 3. Pattern 1: Facade Pattern

### Classification
GoF - Structural Pattern

### Purpose in this project
`FullFlowCalculationService` acts as a Facade, hiding the entire orchestration process of Module 1 -> Module 3 -> Module 4 behind a single method call. The controller only calls one service and has no knowledge of execution order or inter-module dependencies.

### Classes involved

| File | Role |
|------|------|
| `src/main/java/com/drivetrain/fullflow/service/FullFlowCalculationService.java` | **Facade** - orchestrates 3 module services |
| `src/main/java/com/drivetrain/fullflow/controller/FullFlowCalculationController.java` | Client of the Facade |
| `src/main/java/com/drivetrain/module1/service/Module1CalculationService.java` | Subsystem 1 |
| `src/main/java/com/drivetrain/module3/service/Module3CalculationService.java` | Subsystem 2 |
| `src/main/java/com/drivetrain/module4/service/Module4CalculationService.java` | Subsystem 3 |

### Code evidence

The controller has only 1 dependency:

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

The Facade hides all orchestration logic:

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
        // Step 1 - Module 1: motor selection, transmission ratio distribution
        Module1CalculationResponse module1Response = module1CalculationService.calculate(module1Request);

        // Step 2 - Module 3: bevel gear geometry calculation
        Module3CalculationResponse module3Response = module3CalculationService.calculate(module3Request);

        // Step 3 - Module 4: spur gear geometry calculation
        Module4CalculationResponse module4Response = module4CalculationService.calculate(module4Request);

        return new FullFlowCalculationResponse(caseSummary, module1Response, module3Response, module4Response, ...);
    }
}
```

### Why this is a valid example

- The controller does not know that Module 1 must run before Module 3, or that Module 4 depends on Module 1 results.
- If the calculation order changes (e.g., adding Module 2 in the future), only the Facade needs modification - the controller and mobile client are unaffected.
- The client sends 1 request and receives 1 combined response instead of calling 3 separate APIs.

### Benefits

- **Separation of Concerns:** Controller handles HTTP only; Facade handles orchestration.
- **Extensibility:** Adding a new module only requires modifying the Facade.
- **Testability:** The Facade can be tested independently by mocking the 3 module services.

---

## 4. Pattern 2: Strategy Pattern (Priority-based Value Resolution)

### Classification
GoF - Behavioral Pattern (Priority Cascade variant)

### Purpose in this project
Module 4 needs allowable stress values to verify gear strength. These values can come from 3 different sources in priority order:
1. **REQUEST** - user provides values directly in the API request
2. **MODULE3** - inherited from a previously calculated Module 3 result
3. **DEFAULT** - system default values (600/260/260 MPa)

Each source is a "strategy" for resolving values, and the system selects the first applicable strategy.

### Classes involved

| File | Role |
|------|------|
| `src/main/java/com/drivetrain/module4/service/Module4CalculationService.java` | Contains resolution logic |
| Enum `ValueSource` (line 374-379) | Identifies which strategy was selected |
| Record `StressResolution` (line 395-401) | Result of the selected strategy |
| Record `ResolvedInputs` (line 381-393) | Aggregates all resolved inputs + source tracking |

### Code evidence

Enum marking the value source:

```java
private enum ValueSource {
    REQUEST,   // Strategy 1: user-provided
    MODULE1,   // Strategy 2: inherited from Module 1
    MODULE3,   // Strategy 3: inherited from Module 3
    DEFAULT    // Strategy 4: system defaults
}
```

Method `resolveStresses()` implements the priority cascade:

```java
private StressResolution resolveStresses(Module4CalculationRequest request, Long designCaseId) {
    // Strategy 1: Request provides values -> highest priority
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

    // Strategy 2: Inherit from Module 3 -> medium priority
    return module3ResultRepository.findByDesignCaseId(designCaseId)
            .map(module3Result -> new StressResolution(
                    scale(module3Result.getAllowableContactStressMpa() != null
                            ? module3Result.getAllowableContactStressMpa() : DEFAULT_ALLOWABLE_CONTACT_STRESS),
                    // ...
                    ValueSource.MODULE3
            ))
            // Strategy 3: Default -> lowest priority
            .orElse(new StressResolution(
                    scale(DEFAULT_ALLOWABLE_CONTACT_STRESS),
                    scale(DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR1),
                    scale(DEFAULT_ALLOWABLE_BENDING_STRESS_GEAR2),
                    ValueSource.DEFAULT
            ));
}
```

Source tracking is used to generate calculation notes in the response:

```java
switch (inputs.stressSource()) {
    case REQUEST -> notes.add("Allowable stresses were provided directly in the Module 4 request payload.");
    case MODULE3 -> notes.add("Allowable stresses were inherited from the Module 3 result...");
    case DEFAULT -> notes.add("Allowable stresses used backend defaults...");
}
```

### Why this is a valid example

- There are clearly 3 strategies for resolving values with a defined priority order.
- The `ValueSource` enum makes strategy selection traceable (audit trail).
- The `StressResolution` record encapsulates both the resolved values and metadata about their origin.
- The same pattern applies to T2, n2, U3 resolution (REQUEST vs MODULE1).

### Benefits

- **Maintainability:** Adding a new data source (e.g., user profile defaults) only requires adding one branch to the cascade.
- **Traceability:** `calculationNotes` in the response tells the user where each value came from.
- **Testability:** Each case can be tested independently (request with/without values, Module 3 exists/missing).

---

## 5. Pattern 3: Builder Pattern

### Classification
GoF - Creational Pattern

### Purpose in this project
Entities like `Module4Result` have 25+ fields. The Builder pattern allows step-by-step object construction that is readable and safe, eliminating the need for massive constructors.

### Classes involved

| File | Role |
|------|------|
| `src/main/java/com/drivetrain/domain/entity/Module4Result.java` | Entity with @Builder (25+ fields) |
| `src/main/java/com/drivetrain/domain/entity/Module1Result.java` | Entity with @Builder (12+ fields) |
| `src/main/java/com/drivetrain/domain/entity/Module3Result.java` | Entity with @Builder (25+ fields) |
| `src/main/java/com/drivetrain/domain/entity/Module4ShaftForce.java` | Entity with @Builder |
| `src/main/java/com/drivetrain/module4/service/Module4CalculationService.java` | Where the builder is used |

### Code evidence

Entity declares Builder:

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

Builder usage in service (Module4CalculationService.java, lines 61-92):

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
        // ... 15+ more fields
        .build();
```

### Why this is a valid example

- `Module4Result` has 25+ fields - using a constructor would be unreadable and error-prone due to parameter ordering.
- Builder allows naming each field explicitly, with IDE autocomplete support.
- `@Builder.Default` is used for fields with default values (e.g., `shaftForces = new ArrayList<>()`).

### Benefits

- **Readability:** Entity construction reads like documentation - no need to remember the order of 25 parameters.
- **Safety:** Parameter order mistakes are impossible (compile-time safety through method names).
- **Flexibility:** Optional fields can be omitted without needing constructor overloads.

---

## 6. Request Flow Diagram

### Full-Flow: POST /api/v1/drivetrain/full-flow/calculate

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

## 7. Before and After Refactoring

**No refactoring was needed because the existing code already contains clear, defensible patterns.**

All 3 patterns were implemented from the start during development:
- Facade was created when implementing the full-flow endpoint.
- Strategy cascade was designed when Module 4 needed to inherit stresses from Module 3.
- Builder has been used consistently for all entities with many fields.

---

## 8. Demo Script for Instructor

### Presentation script (3-5 minutes)

**Opening:**
> "In our project, we use 3 main design patterns to ensure the code has a clear architecture and is maintainable."

**Pattern 1 - Facade:**
> "First is the Facade Pattern. The class `FullFlowCalculationService` acts as a facade, hiding the orchestration of 3 calculation modules. The controller only calls 1 method - it doesn't need to know that Module 1 must run before Module 3, or that Module 4 depends on Module 1 results. The benefit is that when adding a new module, we only modify the Facade without affecting the API layer."

*Open file: `FullFlowCalculationService.java` -> show method `calculate()` with 3 clear steps.*

**Pattern 2 - Strategy:**
> "Second is the Strategy Pattern as a priority cascade. When calculating the spur gear, the system needs allowable stress values. These can come from 3 sources: user-provided in the request, inherited from Module 3, or system defaults. The method `resolveStresses()` selects the first available source and records the origin using the `ValueSource` enum to create an audit trail in the response."

*Open file: `Module4CalculationService.java` -> show method `resolveStresses()` and enum `ValueSource`.*

**Pattern 3 - Builder:**
> "Third is the Builder Pattern. The entity `Module4Result` has over 25 fields. Instead of a massive constructor, we use Builder to construct the object step by step, with each field explicitly named. This prevents parameter ordering mistakes and makes the code readable."

*Open file: `Module4CalculationService.java` lines 61-92 -> show builder chain.*

**Closing:**
> "All 3 patterns solve specific problems in our project - they are not applied for the sake of it. Facade solves orchestration, Strategy solves multi-source input resolution, Builder solves complex object construction."

---

## 9. Evidence Checklist

| # | Pattern | File to open | Key code location |
|---|---------|-------------|-------------------|
| 1 | Facade | `fullflow/service/FullFlowCalculationService.java` | Method `calculate()` - 3-step orchestration |
| 1 | Facade | `fullflow/controller/FullFlowCalculationController.java` | Only 1 dependency, 2 methods |
| 2 | Strategy | `module4/service/Module4CalculationService.java` | Method `resolveStresses()` lines 159-189 |
| 2 | Strategy | `module4/service/Module4CalculationService.java` | Enum `ValueSource` lines 374-379 |
| 2 | Strategy | `module4/service/Module4CalculationService.java` | Record `StressResolution` lines 395-401 |
| 2 | Strategy | `module4/service/Module4CalculationService.java` | Switch `inputs.stressSource()` lines 229-234 |
| 3 | Builder | `domain/entity/Module4Result.java` | Annotation `@Builder` |
| 3 | Builder | `module4/service/Module4CalculationService.java` | Builder chain lines 61-92 |

---

## 10. Conclusion

The Drivetrain Calculator project applies 3 design patterns intentionally and appropriately:

1. **Facade Pattern** solves the problem of orchestrating multiple complex calculation modules, keeping the API layer simple and extensible.

2. **Strategy Pattern (Priority Cascade)** solves the problem of multi-source input resolution with traceability, making the system flexible in determining input values from multiple sources.

3. **Builder Pattern** solves the problem of constructing complex entities with many fields, making the code safe and readable.

Each pattern has concrete evidence in the source code, solves a real problem in the project, and can be demonstrated live to the instructor.
