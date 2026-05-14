package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.Module3ShaftForce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Module3ShaftForceRepository extends JpaRepository<Module3ShaftForce, Long> {

    List<Module3ShaftForce> findByModule3ResultId(Long module3ResultId);
}
