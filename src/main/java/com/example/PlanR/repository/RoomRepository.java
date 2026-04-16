package com.example.PlanR.repository;

import com.example.PlanR.model.Room;
import com.example.PlanR.model.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.capacity >= :capacity AND r.type = :type")
    List<Room> findRoomsByCapacityAndType(@Param("capacity") Integer capacity, @Param("type") RoomType type);
}
