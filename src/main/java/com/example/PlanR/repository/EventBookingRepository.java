package com.example.PlanR.repository;

import com.example.PlanR.model.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {
}
