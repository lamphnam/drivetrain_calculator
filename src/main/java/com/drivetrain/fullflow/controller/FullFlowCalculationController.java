package com.drivetrain.fullflow.controller;

import com.drivetrain.fullflow.dto.FullFlowCalculationRequest;
import com.drivetrain.fullflow.dto.FullFlowCalculationResponse;
import com.drivetrain.fullflow.service.FullFlowCalculationService;
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
@RequestMapping("/api/v1/drivetrain/full-flow")
@RequiredArgsConstructor
@Validated
public class FullFlowCalculationController {

    private final FullFlowCalculationService fullFlowCalculationService;

    @PostMapping("/calculate")
    public FullFlowCalculationResponse calculate(@Valid @RequestBody FullFlowCalculationRequest request) {
        return fullFlowCalculationService.calculate(request);
    }

    @GetMapping("/history/{designCaseId}")
    public FullFlowCalculationResponse getHistory(@PathVariable @Positive Long designCaseId) {
        return fullFlowCalculationService.getHistory(designCaseId);
    }
}
