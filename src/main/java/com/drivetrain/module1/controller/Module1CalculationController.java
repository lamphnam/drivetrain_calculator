package com.drivetrain.module1.controller;

import com.drivetrain.module1.dto.Module1CalculationRequest;
import com.drivetrain.module1.dto.Module1CalculationResponse;
import com.drivetrain.module1.dto.Module1CalculationHistoryItemResponse;
import com.drivetrain.module1.service.Module1CalculationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/module-1")
@RequiredArgsConstructor
@Validated
public class Module1CalculationController {

    private final Module1CalculationService module1CalculationService;

    @PostMapping("/calculate")
    public Module1CalculationResponse calculateFromInput(@Valid @RequestBody Module1CalculationRequest request) {
        return module1CalculationService.calculate(request);
    }

    @GetMapping("/history")
    public List<Module1CalculationHistoryItemResponse> getHistory() {
        return module1CalculationService.getHistory();
    }

    @GetMapping("/history/{designCaseId}")
    public Module1CalculationResponse getHistoryDetail(@PathVariable @Positive Long designCaseId) {
        return module1CalculationService.getCalculation(designCaseId);
    }
}
