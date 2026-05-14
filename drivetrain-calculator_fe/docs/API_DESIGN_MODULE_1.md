# API Design - Module 1

## 1. Purpose and Scope

This document defines the public API contract for Module 1 of the mobile engineering calculator application.

Module 1 covers:

- reading the motor catalog used for selection
- reading system constants and default assumptions used by the calculation flow
- receiving user calculation input
- returning a calculation result that includes motor selection, transmission ratios, and shaft characteristics

This document is intended to align frontend and backend early while the frontend is still integrating against mock APIs. It describes the public contract that backend should implement later, regardless of internal technology choices.

This document covers Module 1 only. It does not define contracts for later modules, persistence workflows, authentication workflows, or administration tools.

## 2. Backend Flexibility and Contract Stability

This section is intentionally explicit. Backend has broad freedom inside the service boundary, but the public contract must remain stable for frontend compatibility.

### 2.1 Backend may change freely

Backend engineers may change these internal concerns without affecting frontend compatibility:

- internal service architecture
- class, module, or package structure
- programming language
- web framework
- database schema
- internal table and column names
- ORM or query strategy
- caching strategy
- persistence approach
- exact location of calculation logic
- internal naming of use cases or domain services
- internal logging, tracing, and observability
- deployment topology
- performance optimization approach

### 2.2 Backend must keep stable

Backend engineers must preserve these contract elements unless frontend and backend agree on a versioned change:

- endpoint purpose
- public request body shape
- public response body shape
- field names
- field meanings
- numeric units and naming conventions
- validation semantics
- error response format
- success and failure case semantics

For clarity, the current published contract also assumes:

- JSON uses `camelCase`
- IDs are strings
- the motor catalog endpoint currently returns `items` only and does not require a `total` field
- the motor `phase` field is currently a string literal, not a numeric phase count
- the Module 1 calculation response currently does not include a top-level `warnings` array

If backend needs to evolve any of the stable items above, that change should be handled through explicit coordination and versioning, not by silently changing the contract.

## 3. Domain Overview

Module 1 supports motor selection and transmission ratio distribution for an engineering calculation workflow.

Current user input:

- `powerKw`: power on drum shaft in kilowatts
- `outputRpm`: output rotational speed in revolutions per minute

System constants currently used by the contract:

- `etaKn`
- `etaD`
- `etaBrc`
- `etaBrt`
- `etaOl`

Supporting reference data:

- motor catalog

Main output groups:

- `selectedMotor`
- `systemEfficiency`
- `requiredMotorPowerKw`
- `transmissionRatios`
- `shafts`

The current mock implementation uses simplified placeholder math so frontend development can move forward. Backend may implement more accurate engineering logic internally, but the published request and response contract must remain consistent.

## 4. Naming Conventions and Units

The following naming and unit rules are part of the public contract:

- use `camelCase` in JSON request and response bodies
- numeric fields should carry unit meaning in the field name when helpful
- use `kW` semantics for power-related fields such as `powerKw`, `ratedPowerKw`, and `requiredMotorPowerKw`
- use `rpm` semantics for rotational speed fields such as `outputRpm` and `ratedRpm`
- use `Nmm` semantics for torque fields such as `torqueNmm`
- do not return ambiguous numeric fields without clear unit meaning in the field name or documentation
- boolean fields, if added later, should be positively and clearly named
- IDs should remain strings unless frontend and backend explicitly agree otherwise
- decimal precision does not need a fixed number of fractional digits, but values must remain numerically meaningful for engineering display and calculation review

Current field naming assumptions:

- efficiency fields use the `eta*` prefix
- transmission ratio fields use descriptive names such as `beltU1`, `bevelGearU2`, and `spurGearU3`
- `requestId` is a trace identifier for one calculation request, not a persisted business ID

## 5. Authentication Assumptions

Current scope assumes no authentication requirement for Module 1 integration.

If authentication is added later:

- frontend and backend should coordinate the change explicitly
- backward-compatible rollout or versioning should be considered
- authentication changes should not silently alter the request and response body contract for existing Module 1 consumers

## 6. Error Handling Contract

All non-success responses should use one stable error shape:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Input payload is invalid.",
    "details": {
      "context": "optional"
    },
    "fieldErrors": [
      {
        "field": "input.powerKw",
        "reason": "Must be greater than 0."
      }
    ]
  }
}
```

### Error shape semantics

- `error.code`: stable machine-readable discriminator for application logic
- `error.message`: human-readable summary suitable for logs and fallback UI messaging
- `error.details`: optional structured context object
- `error.fieldErrors`: optional array used for request validation problems

### Field error semantics

Each `fieldErrors` item uses:

- `field`: request field path, for example `input.powerKw`
- `reason`: human-readable validation reason

### Published error codes in the current contract

The current mock-backed contract publishes these stable codes:

- `VALIDATION_ERROR`
- `NO_SUITABLE_MOTOR`
- `NOT_FOUND`
- `INTERNAL_ERROR`

### Recommended semantic mapping

For backend clarity:

- use `VALIDATION_ERROR` when request payload structure or value constraints fail
- use `NO_SUITABLE_MOTOR` when the calculation input is valid but no motor in the catalog satisfies the published selection criteria
- use `NOT_FOUND` for missing routes or unsupported resources if needed
- use `INTERNAL_ERROR` for unexpected failures that are not attributable to caller input

The business meaning requested by product for "motor not found" is currently represented by `NO_SUITABLE_MOTOR` in the published contract. Backend should preserve that code for compatibility rather than replacing it with `MOTOR_NOT_FOUND` unless a versioned contract update is agreed.

Similarly, semantics such as "constants not available" or "calculation failed" may be implemented internally, but if backend exposes new public error codes such as `CONSTANTS_NOT_AVAILABLE` or `CALCULATION_FAILED`, frontend coordination is required first.

## 7. API Endpoints

### 7.1 GET /catalog/motors

Purpose:

- return the motor catalog that Module 1 may use for motor selection

Request:

- no request body

Success response shape:

```json
{
  "items": [
    {
      "id": "motor-003",
      "code": "MTR-3P-1.50-1450",
      "name": "Three-phase induction motor 1.50kW 1450rpm",
      "ratedPowerKw": 1.5,
      "ratedRpm": 1450,
      "phase": "3-phase",
      "metadata": {
        "manufacturer": "Mock Drives Co.",
        "frameSize": "90L",
        "efficiencyClass": "IE2"
      }
    }
  ]
}
```

Response field documentation:

- `items`: required array of motor catalog entries; must never be `null`; may be empty

Motor catalog item fields:

- `id`: required string identifier for the motor catalog entry
- `code`: required string motor code
- `name`: required string display name
- `ratedPowerKw`: required number, motor rated power in kilowatts
- `ratedRpm`: required number, motor rated speed in rpm
- `phase`: required string in the current contract; frontend currently expects `"3-phase"`
- `metadata`: optional object with additional non-critical display or filtering hints

Metadata semantics:

- frontend only relies on documented fields above
- backend may add extra metadata properties as long as additions are non-breaking
- frontend should not rely on undocumented metadata keys

Current compatibility note:

- the live contract currently does not include a `total` field
- adding `total` later would be backward-compatible if coordinated, but frontend must not assume it exists today

### 7.2 GET /system/constants/module-1

Purpose:

- return system constants and default assumptions for Module 1

Success response shape:

```json
{
  "etaKn": 0.97,
  "etaD": 0.96,
  "etaBrc": 0.99,
  "etaBrt": 0.98,
  "etaOl": 0.99,
  "defaultBeltRatioUd": 3.2,
  "defaultGearboxRatioUhPreview": 12.5,
  "notes": [
    "Mock constants for frontend integration only.",
    "Engineering formulas and default assumptions must later be aligned with the approved specification."
  ]
}
```

Response field documentation:

- `etaKn`: required default efficiency constant for Module 1
- `etaD`: required default efficiency constant for Module 1
- `etaBrc`: required default efficiency constant for Module 1
- `etaBrt`: required default efficiency constant for Module 1
- `etaOl`: required default efficiency constant for Module 1
- `defaultBeltRatioUd`: required default belt transmission ratio assumption
- `defaultGearboxRatioUhPreview`: required preview gearbox ratio assumption used for frontend guidance
- `notes`: required array of strings in the current contract; may be empty; used for explanatory or assumption-related notes

Semantics:

- these values are defaults and assumptions unless the calculation request overrides them
- backend may source them from configuration, static files, a database, or computed rules
- frontend treats the documented field names and meanings as stable

### 7.3 POST /calculations/module-1

Purpose:

- accept user input and optional overrides
- perform the Module 1 calculation
- return the result contract used by frontend

Request shape:

```json
{
  "input": {
    "powerKw": 1.2,
    "outputRpm": 48
  },
  "optionalOverrides": {
    "etaKn": 0.97,
    "etaD": 0.96,
    "etaBrc": 0.99,
    "etaBrt": 0.98,
    "etaOl": 0.99,
    "beltRatioUd": 3.2,
    "gearboxRatioPreviewUh": 12.5
  }
}
```

Request field documentation:

- `input`: required object
- `input.powerKw`: required number, must be greater than `0`
- `input.outputRpm`: required number, must be greater than `0`
- `optionalOverrides`: optional object
- `optionalOverrides.etaKn`: optional number, if provided must be greater than `0` and less than or equal to `1`
- `optionalOverrides.etaD`: optional number, if provided must be greater than `0` and less than or equal to `1`
- `optionalOverrides.etaBrc`: optional number, if provided must be greater than `0` and less than or equal to `1`
- `optionalOverrides.etaBrt`: optional number, if provided must be greater than `0` and less than or equal to `1`
- `optionalOverrides.etaOl`: optional number, if provided must be greater than `0` and less than or equal to `1`
- `optionalOverrides.beltRatioUd`: optional number, if provided must be greater than `0`
- `optionalOverrides.gearboxRatioPreviewUh`: optional number, if provided must be greater than `0`

Success response shape:

```json
{
  "requestId": "module1-1712040000000",
  "inputEcho": {
    "powerKw": 1.2,
    "outputRpm": 48
  },
  "selectedMotor": {
    "id": "motor-003",
    "code": "MTR-3P-1.50-1450",
    "name": "Three-phase induction motor 1.50kW 1450rpm",
    "ratedPowerKw": 1.5,
    "ratedRpm": 1450,
    "phase": "3-phase",
    "metadata": {
      "manufacturer": "Mock Drives Co.",
      "frameSize": "90L",
      "efficiencyClass": "IE2"
    }
  },
  "systemEfficiency": 0.8942,
  "requiredMotorPowerKw": 1.3419,
  "transmissionRatios": {
    "total": 30.2083,
    "beltU1": 3.2,
    "bevelGearU2": 3.0726,
    "spurGearU3": 3.0727
  },
  "shafts": {
    "motor": {
      "powerKw": 1.2,
      "rpm": 1450,
      "torqueNmm": 7903.4483,
      "summary": "Motor shaft",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    },
    "shaft1": {
      "powerKw": 1.2,
      "rpm": 453.125,
      "torqueNmm": 25282.7586,
      "summary": "Shaft 1 after belt stage",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    },
    "shaft2": {
      "powerKw": 1.2,
      "rpm": 147.4776,
      "torqueNmm": 77639.0071,
      "summary": "Shaft 2 after bevel gear stage",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    },
    "shaft3": {
      "powerKw": 1.2,
      "rpm": 48,
      "torqueNmm": 238750,
      "summary": "Shaft 3 after spur gear stage",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    },
    "drumShaft": {
      "powerKw": 1.2,
      "rpm": 48,
      "torqueNmm": 238750,
      "summary": "Output drum shaft",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    }
  }
}
```

Response field documentation:

- `requestId`: required string request trace identifier
- `inputEcho`: required sanitized echo of the accepted request input; useful for result rendering and traceability
- `selectedMotor`: required chosen motor catalog entry
- `systemEfficiency`: required number representing the combined efficiency used for this result
- `requiredMotorPowerKw`: required number representing the calculated motor power requirement after efficiency adjustments
- `transmissionRatios`: required object containing the overall and stage-level transmission ratios
- `shafts`: required object containing shaft characteristic summaries for the calculation result

`transmissionRatios` fields:

- `total`: required overall transmission ratio
- `beltU1`: required belt stage ratio
- `bevelGearU2`: required bevel gear stage ratio
- `spurGearU3`: required spur gear stage ratio

`shafts` object fields:

- `motor`: required shaft result for motor shaft
- `shaft1`: required shaft result for stage 1
- `shaft2`: required shaft result for stage 2
- `shaft3`: required shaft result for stage 3
- `drumShaft`: required shaft result for output drum shaft

Each shaft object includes:

- `powerKw`: required shaft power in kilowatts
- `rpm`: required shaft rotational speed in rpm
- `torqueNmm`: required shaft torque in newton-millimeters
- `summary`: optional short description; frontend may render this directly when present
- `warnings`: optional string array; warnings are currently defined at shaft level, not top level
- `metadata`: optional light object; frontend must not rely on undocumented metadata keys

Notes on frontend rendering:

- frontend is expected to render `selectedMotor`, `systemEfficiency`, `requiredMotorPowerKw`, `transmissionRatios`, and `shafts`
- `inputEcho` allows frontend to show the exact accepted inputs used for the result
- `requestId` is useful for diagnostics, audit trails, and support workflows
- the current public contract does not include a top-level `warnings` array; if that is needed later it should be added in a coordinated backward-compatible way

## 8. DTO Definitions

### MotorCatalogItemDto

Purpose:

- represents one motor entry returned by the catalog endpoint and used by the calculation response

Fields:

- `id`: required string; stable motor identifier; frontend may use for list keys and references
- `code`: required string; motor code
- `name`: required string; display label
- `ratedPowerKw`: required number; rated motor power in kW; frontend treats as required for rendering
- `ratedRpm`: required number; rated motor speed in rpm; frontend treats as required for rendering
- `phase`: required string literal in the current contract; frontend treats as required for rendering
- `metadata`: optional object; additional non-critical information

### Module1ConstantsDto

Purpose:

- represents default constants and assumptions for Module 1

Fields:

- `etaKn`: required number; default efficiency
- `etaD`: required number; default efficiency
- `etaBrc`: required number; default efficiency
- `etaBrt`: required number; default efficiency
- `etaOl`: required number; default efficiency
- `defaultBeltRatioUd`: required number; default belt ratio
- `defaultGearboxRatioUhPreview`: required number; preview gearbox ratio assumption
- `notes`: required string array in the current contract; may be empty

Frontend rendering note:

- frontend may display these values and notes directly when presenting assumptions or defaults

### Module1CalculationInputDto

Purpose:

- captures the required user-provided calculation inputs

Fields:

- `powerKw`: required number; output power requirement in kW
- `outputRpm`: required number; output rotational speed in rpm

### Module1CalculationOverridesDto

Purpose:

- captures optional caller-provided overrides for default assumptions

Fields:

- `etaKn`: optional number
- `etaD`: optional number
- `etaBrc`: optional number
- `etaBrt`: optional number
- `etaOl`: optional number
- `beltRatioUd`: optional number
- `gearboxRatioPreviewUh`: optional number

Frontend rendering note:

- frontend may allow the user to edit these values, but should not assume any override is required

### Module1CalculationRequestDto

Purpose:

- request DTO for `POST /calculations/module-1`

Fields:

- `input`: required `Module1CalculationInputDto`
- `optionalOverrides`: optional `Module1CalculationOverridesDto`

### Module1SelectedMotorDto

Purpose:

- selected motor returned as part of the Module 1 calculation result

Contract note:

- this is semantically the same shape as `MotorCatalogItemDto` in the current contract
- frontend may treat it as required for rendering the result summary

Fields:

- same fields as `MotorCatalogItemDto`

### Module1TransmissionRatiosDto

Purpose:

- groups the calculated transmission ratio values

Fields:

- `total`: required number; total transmission ratio
- `beltU1`: required number; belt stage ratio
- `bevelGearU2`: required number; bevel gear stage ratio
- `spurGearU3`: required number; spur gear stage ratio

Frontend rendering note:

- frontend treats all four fields as required for result rendering

### Module1ShaftDto

Purpose:

- describes one shaft result in the calculation response

Fields:

- `powerKw`: required number; shaft power in kW
- `rpm`: required number; shaft speed in rpm
- `torqueNmm`: required number; shaft torque in Nmm
- `summary`: optional string; human-readable description
- `warnings`: optional string array; shaft-specific warnings
- `metadata`: optional object; extra non-critical data

Frontend rendering note:

- frontend considers `powerKw`, `rpm`, and `torqueNmm` mandatory for rendering
- `summary`, `warnings`, and `metadata` should be treated as optional enhancements

### Module1CalculationResponseDto

Purpose:

- success response DTO for `POST /calculations/module-1`

Fields:

- `requestId`: required string
- `inputEcho`: required `Module1CalculationInputDto`
- `selectedMotor`: required `Module1SelectedMotorDto`
- `systemEfficiency`: required number
- `requiredMotorPowerKw`: required number
- `transmissionRatios`: required `Module1TransmissionRatiosDto`
- `shafts`: required object containing `motor`, `shaft1`, `shaft2`, `shaft3`, and `drumShaft`, each of type `Module1ShaftDto`

### ApiErrorResponseDto

Purpose:

- standard error wrapper for all non-success responses

Fields:

- `error`: required object

Nested `error` fields:

- `code`: required stable machine-readable error code
- `message`: required human-readable summary
- `details`: optional structured context object
- `fieldErrors`: optional `FieldErrorDto[]`

### FieldErrorDto

Purpose:

- provides field-level validation detail for request errors

Fields:

- `field`: required string path to the invalid field
- `reason`: required human-readable explanation

## 9. Validation Rules

The following validation semantics are part of the public contract:

- `input.powerKw` must be greater than `0`
- `input.outputRpm` must be greater than `0`
- override efficiencies, if provided, must be greater than `0` and less than or equal to `1`
- `beltRatioUd`, if provided, must be greater than `0`
- `gearboxRatioPreviewUh`, if provided, must be greater than `0`
- `GET /catalog/motors` should return a non-null list; the list may be empty
- if no suitable motor exists, return the stable domain error code `NO_SUITABLE_MOTOR`
- if constants are unavailable, backend may fail internally, but introducing a new public error code such as `CONSTANTS_NOT_AVAILABLE` requires frontend coordination
- if calculation cannot complete for an unexpected reason, use `INTERNAL_ERROR` in the current published contract unless a new versioned error code is agreed

Validation compatibility rule:

- backend may validate more strictly internally
- any stricter public validation behavior must remain backward-compatible or be documented and coordinated before rollout

## 10. Example Requests and Responses

### 10.1 GET /catalog/motors success example

```json
{
  "items": [
    {
      "id": "motor-002",
      "code": "MTR-3P-1.10-960",
      "name": "Three-phase induction motor 1.10kW 960rpm",
      "ratedPowerKw": 1.1,
      "ratedRpm": 960,
      "phase": "3-phase",
      "metadata": {
        "manufacturer": "Mock Drives Co.",
        "frameSize": "90S",
        "efficiencyClass": "IE2"
      }
    },
    {
      "id": "motor-003",
      "code": "MTR-3P-1.50-1450",
      "name": "Three-phase induction motor 1.50kW 1450rpm",
      "ratedPowerKw": 1.5,
      "ratedRpm": 1450,
      "phase": "3-phase",
      "metadata": {
        "manufacturer": "Mock Drives Co.",
        "frameSize": "90L",
        "efficiencyClass": "IE2"
      }
    }
  ]
}
```

### 10.2 GET /system/constants/module-1 success example

```json
{
  "etaKn": 0.97,
  "etaD": 0.96,
  "etaBrc": 0.99,
  "etaBrt": 0.98,
  "etaOl": 0.99,
  "defaultBeltRatioUd": 3.2,
  "defaultGearboxRatioUhPreview": 12.5,
  "notes": [
    "Mock constants for frontend integration only.",
    "Engineering formulas and default assumptions must later be aligned with the approved specification."
  ]
}
```

### 10.3 POST /calculations/module-1 success example

Request:

```json
{
  "input": {
    "powerKw": 1.2,
    "outputRpm": 48
  },
  "optionalOverrides": {
    "beltRatioUd": 3.2
  }
}
```

Response:

```json
{
  "requestId": "module1-1712040000000",
  "inputEcho": {
    "powerKw": 1.2,
    "outputRpm": 48
  },
  "selectedMotor": {
    "id": "motor-003",
    "code": "MTR-3P-1.50-1450",
    "name": "Three-phase induction motor 1.50kW 1450rpm",
    "ratedPowerKw": 1.5,
    "ratedRpm": 1450,
    "phase": "3-phase",
    "metadata": {
      "manufacturer": "Mock Drives Co.",
      "frameSize": "90L",
      "efficiencyClass": "IE2"
    }
  },
  "systemEfficiency": 0.8942,
  "requiredMotorPowerKw": 1.3419,
  "transmissionRatios": {
    "total": 30.2083,
    "beltU1": 3.2,
    "bevelGearU2": 3.0726,
    "spurGearU3": 3.0727
  },
  "shafts": {
    "motor": {
      "powerKw": 1.2,
      "rpm": 1450,
      "torqueNmm": 7903.4483,
      "summary": "Motor shaft",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    },
    "shaft1": {
      "powerKw": 1.2,
      "rpm": 453.125,
      "torqueNmm": 25282.7586,
      "summary": "Shaft 1 after belt stage",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    },
    "shaft2": {
      "powerKw": 1.2,
      "rpm": 147.4776,
      "torqueNmm": 77639.0071,
      "summary": "Shaft 2 after bevel gear stage",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    },
    "shaft3": {
      "powerKw": 1.2,
      "rpm": 48,
      "torqueNmm": 238750,
      "summary": "Shaft 3 after spur gear stage",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    },
    "drumShaft": {
      "powerKw": 1.2,
      "rpm": 48,
      "torqueNmm": 238750,
      "summary": "Output drum shaft",
      "metadata": {
        "formulaStatus": "placeholder-math"
      }
    }
  }
}
```

### 10.4 POST /calculations/module-1 validation error example

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed.",
    "fieldErrors": [
      {
        "field": "input.powerKw",
        "reason": "powerKw must be greater than 0 kW."
      },
      {
        "field": "optionalOverrides.etaKn",
        "reason": "Efficiency overrides must be in the range (0, 1]."
      }
    ]
  }
}
```

### 10.5 POST /calculations/module-1 no suitable motor example

```json
{
  "error": {
    "code": "NO_SUITABLE_MOTOR",
    "message": "No suitable motor was found for the required power and speed range.",
    "details": {
      "requiredMotorPowerKw": 9.2
    }
  }
}
```

## 11. Frontend Integration Notes

Frontend developers should treat the following as safe to rely on:

- documented endpoint paths
- documented request and response field names
- documented field meanings and units
- documented error codes and error shape
- required fields described in the DTO section

Frontend should also follow these rules:

- do not depend on undocumented extra fields
- treat `metadata`, shaft-level `warnings`, and `summary` fields as optional unless documented otherwise
- handle error cases primarily by `error.code`
- treat the response shape in this document as the canonical Module 1 integration contract
- avoid parsing meaning from free-form `message` text when `error.code` is available

## 12. Backend Implementation Notes

Backend engineers may implement this contract using any appropriate approach, including:

- direct calculation services without persistence
- configuration files for constants
- static or seeded catalog tables
- relational or non-relational databases
- synchronous or asynchronous internal orchestration

Backend engineers may change internal models freely, but should preserve the public API shape defined here.

If backend needs to evolve the contract:

- coordinate with frontend first
- prefer additive backward-compatible changes
- use versioning or an agreed migration plan for breaking changes

## 13. Future Compatibility Notes

This API covers Module 1 only. Future modules are expected to build on top of the structured output from Module 1.

Compatibility guidance for future expansion:

- prefer additive changes over breaking field renames
- keep Module 1 response fields stable so later modules can treat Module 1 output as source-of-truth input
- new optional fields are generally safer than changing existing required fields
- if pagination, richer warnings, or more detailed result breakdowns are added later, they should be introduced in a backward-compatible way
- if field semantics must change, use explicit versioning rather than silent contract drift
