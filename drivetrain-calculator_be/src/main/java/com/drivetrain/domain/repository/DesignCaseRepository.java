package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.DesignCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DesignCaseRepository extends JpaRepository<DesignCase, Long> {

    boolean existsByCaseCode(String caseCode);

    Optional<DesignCase> findByCaseCode(String caseCode);
}
