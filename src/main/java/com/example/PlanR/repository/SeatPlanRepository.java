package com.example.PlanR.repository;

import com.example.PlanR.model.SeatPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SeatPlanRepository extends JpaRepository<SeatPlan, Long> {
    Optional<SeatPlan> findTopByRoomIdOrderByGeneratedAtDesc(Long roomId);
}