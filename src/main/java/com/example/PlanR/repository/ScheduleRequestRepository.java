package com.example.PlanR.repository;

import com.example.PlanR.model.ScheduleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRequestRepository extends JpaRepository<ScheduleRequest, Long> {
}
