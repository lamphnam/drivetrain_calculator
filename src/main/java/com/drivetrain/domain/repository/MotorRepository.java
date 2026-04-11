package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.Motor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MotorRepository extends JpaRepository<Motor, Long> {

    List<Motor> findByIsActiveTrue();

    boolean existsByMotorCode(String motorCode);

    List<Motor> findByIsActiveTrueAndRatedPowerKwGreaterThanEqualOrderByRatedRpmAsc(BigDecimal ratedPowerKw);
}
