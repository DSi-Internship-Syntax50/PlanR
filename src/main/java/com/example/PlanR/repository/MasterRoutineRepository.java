package com.example.PlanR.repository;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface MasterRoutineRepository extends JpaRepository<MasterRoutine, Long> {

    // --- Methods from Ai-FAQ branch ---
    List<MasterRoutine> findByDayOfWeekAndStartTimeBetween(DayOfWeek dayOfWeek, LocalTime start, LocalTime end);
    
    List<MasterRoutine> findByCourseBatch(String batch);

    // --- Methods from dev branch ---
    @Query("SELECT m FROM MasterRoutine m WHERE m.room.id = :roomId AND m.dayOfWeek = :dayOfWeek AND " +
           "m.startSlotIndex <= :endSlot AND (m.startSlotIndex + m.course.slotCount - 1) >= :startSlot")
    List<MasterRoutine> findOverlappingRoutines(
            @Param("roomId") Long roomId, 
            @Param("dayOfWeek") DayOfWeek dayOfWeek, 
            @Param("startSlot") Integer startSlot, 
            @Param("endSlot") Integer endSlot
    );

    List<MasterRoutine> findByTeacherIdAndDayOfWeekOrderByStartSlotIndexAsc(Long teacherId, DayOfWeek dayOfWeek);

    List<MasterRoutine> findByRoomId(Long roomId);

    List<MasterRoutine> findByRoomIsNull();
}