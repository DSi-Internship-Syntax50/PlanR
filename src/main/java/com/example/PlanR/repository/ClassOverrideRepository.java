package com.example.PlanR.repository;

import com.example.PlanR.model.ClassOverride;
import com.example.PlanR.model.enums.OverrideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassOverrideRepository extends JpaRepository<ClassOverride, Long> {

    List<ClassOverride> findBySpecificDateAndRoutineIdIn(LocalDate date, List<Long> routineIds);
}
