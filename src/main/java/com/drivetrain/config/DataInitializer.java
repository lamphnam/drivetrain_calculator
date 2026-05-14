package com.drivetrain.config;

import com.drivetrain.domain.entity.DesignConstantSet;
import com.drivetrain.domain.entity.GearMaterial;
import com.drivetrain.domain.entity.Motor;
import com.drivetrain.domain.repository.DesignConstantSetRepository;
import com.drivetrain.domain.repository.GearMaterialRepository;
import com.drivetrain.domain.repository.MotorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final DesignConstantSetRepository constantSetRepository;
    private final MotorRepository motorRepository;
    private final GearMaterialRepository gearMaterialRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedConstantSet();
        seedMotors();
        seedGearMaterials();
    }

    private void seedConstantSet() {
        if (constantSetRepository.findBySetCode("DEFAULT_SET_V1").isPresent()) {
            log.info("[Seed] DesignConstantSet 'DEFAULT_SET_V1' already exists, skipping.");
            return;
        }

        DesignConstantSet constantSet = DesignConstantSet.builder()
                .setCode("DEFAULT_SET_V1")
                .setName("Default Module 1 Constants")
                .etaKn(new BigDecimal("1.000000"))
                .etaD(new BigDecimal("0.955000"))
                .etaBrc(new BigDecimal("0.960000"))
                .etaBrt(new BigDecimal("0.970000"))
                .etaOl(new BigDecimal("0.995000"))
                .defaultBeltRatioU1(new BigDecimal("3.600000"))
                .defaultGearboxRatioUh(new BigDecimal("11.500000"))
                .isActive(true)
                .build();

        constantSetRepository.save(constantSet);
        log.info("[Seed] Created DesignConstantSet: {}", constantSet.getSetCode());
    }

    private void seedMotors() {
        seedMotor("4A80B2Y6", 2.2, 2880, "Standard", "2.2 kW, 2880 rpm");
        seedMotor("4A90L2Y6", 3.0, 2880, "Standard", "3.0 kW, 2880 rpm");
        seedMotor("4A100S2Y6", 4.0, 2880, "Standard", "4.0 kW, 2880 rpm");
        seedMotor("4A100L2Y6", 5.5, 2880, "Standard", "5.5 kW, 2880 rpm");
        seedMotor("4A112M2Y6", 7.5, 2922, "Standard", "7.5 kW, 2922 rpm - reference motor");
        seedMotor("4A132S2Y6", 11.0, 2900, "Standard", "11.0 kW, 2900 rpm");
        seedMotor("4A132M2Y6", 15.0, 2900, "Standard", "15.0 kW, 2900 rpm");
        seedMotor("4A160S2Y6", 18.5, 2940, "Standard", "18.5 kW, 2940 rpm");
        seedMotor("4A160M2Y6", 22.0, 2940, "Standard", "22.0 kW, 2940 rpm");
    }

    private void seedMotor(String motorCode, double ratedPowerKw, double ratedRpm, String manufacturer, String description) {
        if (motorRepository.existsByMotorCode(motorCode)) {
            log.debug("[Seed] Motor '{}' already exists, skipping.", motorCode);
            return;
        }

        Motor motor = Motor.builder()
                .motorCode(motorCode)
                .ratedPowerKw(BigDecimal.valueOf(ratedPowerKw))
                .ratedRpm(BigDecimal.valueOf(ratedRpm))
                .manufacturer(manufacturer)
                .description(description)
                .isActive(true)
                .build();

        motorRepository.save(motor);
        log.info("[Seed] Created Motor: {}", motorCode);
    }

    private void seedGearMaterials() {
        seedGearMaterial("C40XH_QT", "Steel C40XH", "Quenched and tempered", 235, 262, 850, 650);
        seedGearMaterial("40CR_N", "Steel 40Cr", "Normalized", 207, 241, 780, 540);
        seedGearMaterial("C45_N", "Steel C45", "Normalized", 179, 207, 600, 355);
    }

    private void seedGearMaterial(
            String materialCode,
            String materialName,
            String heatTreatment,
            double hbMin,
            double hbMax,
            double sigmaBMpa,
            double sigmaChMpa
    ) {
        if (gearMaterialRepository.existsByMaterialCode(materialCode)) {
            log.debug("[Seed] Gear material '{}' already exists, skipping.", materialCode);
            return;
        }

        GearMaterial material = GearMaterial.builder()
                .materialCode(materialCode)
                .materialName(materialName)
                .heatTreatment(heatTreatment)
                .hbMin(BigDecimal.valueOf(hbMin))
                .hbMax(BigDecimal.valueOf(hbMax))
                .sigmaBMpa(BigDecimal.valueOf(sigmaBMpa))
                .sigmaChMpa(BigDecimal.valueOf(sigmaChMpa))
                .build();

        gearMaterialRepository.save(material);
        log.info("[Seed] Created GearMaterial: {}", materialCode);
    }
}
