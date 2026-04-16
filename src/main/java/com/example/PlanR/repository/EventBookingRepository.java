package com.example.PlanR.repository;

import com.example.PlanR.model.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {

    List<EventBooking> findBySpecificDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT e FROM EventBooking e WHERE e.room.id = :roomId AND e.specificDate = :date " +
           "AND e.status NOT IN ('REJECTED', 'DISPLACED')")
    List<EventBooking> findByRoomIdAndSpecificDate(
            @Param("roomId") Long roomId,
            @Param("date") LocalDate date);

    @Query("SELECT e FROM EventBooking e WHERE e.room.id = :roomId AND e.specificDate = :date " +
           "AND e.startTime < :endTime AND e.endTime > :startTime AND e.status != 'REJECTED' AND e.status != 'DISPLACED'")
    List<EventBooking> findOverlappingBookings(
            @Param("roomId") Long roomId, 
            @Param("date") LocalDate date, 
            @Param("startTime") LocalTime startTime, 
            @Param("endTime") LocalTime endTime);
    @Query("SELECT b FROM EventBooking b WHERE b.specificDate BETWEEN :startDate AND :endDate " +
           "AND (:isAdmin = true OR b.status = 'APPROVED' OR b.requestedBy.email = :username)")
    List<EventBooking> findVisibleBookingsForMonth(@Param("startDate") java.time.LocalDate startDate, 
                                                   @Param("endDate") java.time.LocalDate endDate, 
                                                   @Param("username") String username, 
                                                   @Param("isAdmin") boolean isAdmin);
}
