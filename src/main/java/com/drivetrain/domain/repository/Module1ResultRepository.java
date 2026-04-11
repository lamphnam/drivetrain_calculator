package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.Module1Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Module1ResultRepository extends JpaRepository<Module1Result, Long> {

    Optional<Module1Result> findByDesignCaseId(Long designCaseId);
}
