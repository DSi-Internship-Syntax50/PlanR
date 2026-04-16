package com.example.PlanR.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PlanR.model.ClassOverride;

@Repository
public interface ClassOverrideRepository extends JpaRepository<ClassOverride, Long> {

    List<ClassOverride> findBySpecificDateAndRoutineIdIn(LocalDate date, List<Long> routineIds);
}
