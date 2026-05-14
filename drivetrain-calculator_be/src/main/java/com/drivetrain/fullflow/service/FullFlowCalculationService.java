package com.drivetrain.fullflow.service;

import com.drivetrain.domain.entity.DesignCase;
import com.drivetrain.domain.entity.GearMaterial;
import com.drivetrain.domain.repository.DesignCaseRepository;
import com.drivetrain.domain.repository.GearMaterialRepository;
import com.drivetrain.domain.repository.Module1ResultRepository;
import com.drivetrain.domain.repository.Module3ResultRepository;
import com.drivetrain.domain.repository.Module4ResultRepository;
import com.drivetrain.fullflow.dto.FullFlowCalculationRequest;
import com.drivetrain.fullflow.dto.FullFlowCalculationResponse;
import com.drivetrain.module1.dto.Module1CalculationRequest;
import com.drivetrain.module1.dto.Module1CalculationResponse;
import com.drivetrain.module1.service.Module1CalculationService;
import com.drivetrain.module3.dto.Module3CalculationRequest;
import com.drivetrain.module3.dto.Module3CalculationResponse;
import com.drivetrain.module3.service.Module3CalculationService;
import com.drivetrain.module4.dto.Module4CalculationRequest;
import com.drivetrain.module4.dto.Module4CalculationResponse;
import com.drivetrain.module4.service.Module4CalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FullFlowCalculationService {

    private static final Long DEFAULT_MATERIAL_ID = 1L;

    private final Module1CalculationService module1CalculationService;
    private final Module3CalculationService module3CalculationService;
    private final Module4CalculationService module4CalculationService;
    private final DesignCaseRepository designCaseRepository;
    private final GearMaterialRepository gearMaterialRepository;
    private final Module1ResultRepository module1ResultRepository;
    private final Module3ResultRepository module3ResultRepository;
    private final Module4ResultRepository module4ResultRepository;

    @Transactional
    public FullFlowCalculationResponse calculate(FullFlowCalculationRequest request) {
        List<String> warnings = new ArrayList<>();

        // Step 1 — Module 1
        Module1CalculationRequest module1Request = new Module1CalculationRequest(
                request.requiredPowerKw(),
                request.requiredOutputRpm(),
                request.constantSetId(),
                request.caseCode(),
                request.caseName()
        );
        Module1CalculationResponse module1Response = module1CalculationService.calculate(module1Request);
        Long designCaseId = module1Response.caseInfo().designCaseId();

        // Step 2 — Module 3
        Long materialId = resolveMaterialId(request.module3MaterialId(), warnings);
        Module3CalculationRequest module3Request = new Module3CalculationRequest(
                designCaseId,
                null,
                null,
                null,
                request.module3ServiceLifeHours(),
                materialId
        );
        Module3CalculationResponse module3Response = module3CalculationService.calculate(module3Request);

        // Step 3 — Module 4
        Module4CalculationRequest module4Request = new Module4CalculationRequest(
                designCaseId,
                null,
                null,
                null,
                request.module4AllowableContactStressMpa(),
                request.module4AllowableBendingStressGear1Mpa(),
                request.module4AllowableBendingStressGear2Mpa()
        );
        Module4CalculationResponse module4Response = module4CalculationService.calculate(module4Request);

        DesignCase designCase = designCaseRepository.findById(designCaseId).orElseThrow();

        FullFlowCalculationResponse.CaseSummary caseSummary = new FullFlowCalculationResponse.CaseSummary(
                designCase.getId(),
                designCase.getCaseCode(),
                designCase.getCaseName()
        );

        return new FullFlowCalculationResponse(
                caseSummary,
                module1Response,
                module3Response,
                module4Response,
                designCase.getStatus().name(),
                warnings
        );
    }

    @Transactional(readOnly = true)
    public FullFlowCalculationResponse getHistory(Long designCaseId) {
        List<String> warnings = new ArrayList<>();

        DesignCase designCase = designCaseRepository.findById(designCaseId)
                .orElseThrow(() -> new IllegalArgumentException("Design case not found: " + designCaseId));

        Module1CalculationResponse module1Response = null;
        Module3CalculationResponse module3Response = null;
        Module4CalculationResponse module4Response = null;

        try {
            module1Response = module1CalculationService.getCalculation(designCaseId);
        } catch (Exception e) {
            warnings.add("Module 1 result not available: " + e.getMessage());
        }

        try {
            module3Response = module3CalculationService.getCalculation(designCaseId);
        } catch (Exception e) {
            warnings.add("Module 3 result not available: " + e.getMessage());
        }

        try {
            module4Response = module4CalculationService.getCalculation(designCaseId);
        } catch (Exception e) {
            warnings.add("Module 4 result not available: " + e.getMessage());
        }

        FullFlowCalculationResponse.CaseSummary caseSummary = new FullFlowCalculationResponse.CaseSummary(
                designCase.getId(),
                designCase.getCaseCode(),
                designCase.getCaseName()
        );

        return new FullFlowCalculationResponse(
                caseSummary,
                module1Response,
                module3Response,
                module4Response,
                designCase.getStatus().name(),
                warnings
        );
    }

    private Long resolveMaterialId(Long requestedMaterialId, List<String> warnings) {
        if (requestedMaterialId != null) {
            return requestedMaterialId;
        }
        return gearMaterialRepository.findAll().stream()
                .findFirst()
                .map(GearMaterial::getId)
                .orElseGet(() -> {
                    warnings.add("No gear materials found in database, using default material ID " + DEFAULT_MATERIAL_ID);
                    return DEFAULT_MATERIAL_ID;
                });
    }
}
