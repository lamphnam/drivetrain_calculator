package com.drivetrain.module3.controller;

import com.drivetrain.module3.dto.GearMaterialReferenceResponse;
import com.drivetrain.module3.dto.Module3CalculationRequest;
import com.drivetrain.module3.dto.Module3CalculationResponse;
import com.drivetrain.module3.service.Module3CalculationService;
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
@RequestMapping("/api/v1/module-3")
@RequiredArgsConstructor
@Validated
public class Module3CalculationController {

    private final Module3CalculationService module3CalculationService;

    @PostMapping("/calculate")
    public Module3CalculationResponse calculate(@Valid @RequestBody Module3CalculationRequest request) {
        return module3CalculationService.calculate(request);
    }

    @GetMapping("/materials")
    public List<GearMaterialReferenceResponse> getMaterials() {
        return module3CalculationService.getMaterials();
    }

    @GetMapping("/history/{designCaseId}")
    public Module3CalculationResponse getCalculation(@PathVariable @Positive Long designCaseId) {
        return module3CalculationService.getCalculation(designCaseId);
    }
}
