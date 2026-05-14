package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.Module4Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Module4ResultRepository extends JpaRepository<Module4Result, Long> {

    Optional<Module4Result> findByDesignCaseId(Long designCaseId);

    @Query("""
            select distinct result
            from Module4Result result
            join fetch result.designCase designCase
            left join fetch result.shaftForces shaftForce
            where designCase.id = :designCaseId
            """)
    Optional<Module4Result> findDetailedByDesignCaseId(@Param("designCaseId") Long designCaseId);
}
