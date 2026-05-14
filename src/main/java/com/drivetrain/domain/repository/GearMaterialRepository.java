package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.GearMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GearMaterialRepository extends JpaRepository<GearMaterial, Long> {

    boolean existsByMaterialCode(String materialCode);

    Optional<GearMaterial> findByMaterialCode(String materialCode);

    List<GearMaterial> findAllByOrderByMaterialCodeAsc();
}
