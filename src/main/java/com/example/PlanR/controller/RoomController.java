package com.example.PlanR.controller;

import com.example.PlanR.dto.RoomCreateDto;
import com.example.PlanR.model.Room;
import com.example.PlanR.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'COORDINATOR')")
    public ResponseEntity<Room> createRoom(@RequestBody RoomCreateDto dto) {
        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setType(dto.getType());
        room.setCapacity(dto.getCapacity());
        room.setHasComputers(dto.getHasComputers() != null ? dto.getHasComputers() : false);
        room.setHasHardwareKits(dto.getHasHardwareKits() != null ? dto.getHasHardwareKits() : false);
        room.setFloorNumber(dto.getFloorNumber());
        room.setBlock(dto.getBlock());
        room.setDept(dto.getDept());

        Room savedRoom = roomRepository.save(room);
        return ResponseEntity.ok(savedRoom);
    }
}
