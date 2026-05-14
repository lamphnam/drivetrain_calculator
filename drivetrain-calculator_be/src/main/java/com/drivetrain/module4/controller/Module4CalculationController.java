package com.drivetrain.module4.controller;

import com.drivetrain.module4.dto.Module4CalculationRequest;
import com.drivetrain.module4.dto.Module4CalculationResponse;
import com.drivetrain.module4.service.Module4CalculationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/module-4")
@RequiredArgsConstructor
@Validated
public class Module4CalculationController {

    private final Module4CalculationService module4CalculationService;

    @PostMapping("/calculate")
    public Module4CalculationResponse calculate(@Valid @RequestBody Module4CalculationRequest request) {
        return module4CalculationService.calculate(request);
    }

    @GetMapping("/history/{designCaseId}")
    public Module4CalculationResponse getCalculation(@PathVariable @Positive Long designCaseId) {
        return module4CalculationService.getCalculation(designCaseId);
    }
}
