package com.example.PlanR.repository;

import com.example.PlanR.model.MasterRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterRoutineRepository extends JpaRepository<MasterRoutine, Long> {
}
