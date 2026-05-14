package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.Module3Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Module3ResultRepository extends JpaRepository<Module3Result, Long> {

    Optional<Module3Result> findByDesignCaseId(Long designCaseId);

    @Query("""
            select distinct result
            from Module3Result result
            join fetch result.designCase designCase
            join fetch result.material material
            left join fetch result.shaftForces shaftForce
            where designCase.id = :designCaseId
            """)
    Optional<Module3Result> findDetailedByDesignCaseId(@Param("designCaseId") Long designCaseId);
}
