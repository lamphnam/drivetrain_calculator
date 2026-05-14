package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.DesignConstantSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignConstantSetRepository extends JpaRepository<DesignConstantSet, Long> {

    Optional<DesignConstantSet> findBySetCode(String setCode);

    List<DesignConstantSet> findByIsActiveTrue();

    Optional<DesignConstantSet> findFirstByIsActiveTrueOrderByIdAsc();
}
