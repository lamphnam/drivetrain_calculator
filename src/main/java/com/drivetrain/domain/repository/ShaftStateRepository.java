package com.drivetrain.domain.repository;

import com.drivetrain.domain.entity.ShaftState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShaftStateRepository extends JpaRepository<ShaftState, Long> {

    List<ShaftState> findByModule1ResultIdOrderBySequenceNoAsc(Long module1ResultId);
}
