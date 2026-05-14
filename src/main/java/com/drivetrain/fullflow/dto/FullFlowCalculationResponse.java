package com.drivetrain.fullflow.dto;

import com.drivetrain.module1.dto.Module1CalculationResponse;
import com.drivetrain.module3.dto.Module3CalculationResponse;
import com.drivetrain.module4.dto.Module4CalculationResponse;

import java.util.List;

public record FullFlowCalculationResponse(
        CaseSummary caseSummary,
        Module1CalculationResponse module1Result,
        Module3CalculationResponse module3Result,
        Module4CalculationResponse module4Result,
        String finalStatus,
        List<String> warnings
) {

    public record CaseSummary(
            Long designCaseId,
            String caseCode,
            String caseName
    ) {
    }
}
