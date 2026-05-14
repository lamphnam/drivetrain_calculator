package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.Module1Result;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Module1ResultRepository extends JpaRepository<Module1Result, Long> {

    Optional<Module1Result> findByDesignCaseId(Long designCaseId);

    @EntityGraph(attributePaths = {"designCase", "selectedMotor"})
    List<Module1Result> findAllByOrderByUpdatedAtDesc();

    @Query("""
            select distinct result
            from Module1Result result
            join fetch result.designCase designCase
            join fetch designCase.constantSet constantSet
            join fetch result.selectedMotor selectedMotor
            left join fetch result.shaftStates shaftState
            where designCase.id = :designCaseId
            """)
    Optional<Module1Result> findDetailedByDesignCaseId(@Param("designCaseId") Long designCaseId);
}
