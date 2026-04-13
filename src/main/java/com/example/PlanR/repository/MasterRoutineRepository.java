package com.example.PlanR.repository;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface MasterRoutineRepository extends JpaRepository<MasterRoutine, Long> {
    List<MasterRoutine> findByDayOfWeekAndStartTimeBetween(DayOfWeek dayOfWeek, LocalTime start, LocalTime end);
    List<MasterRoutine> findByCourseBatch(String batch);
}
