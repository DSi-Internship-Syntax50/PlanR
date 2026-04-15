package com.example.PlanR.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PlanR.dto.RoomCreateDto;
import com.example.PlanR.model.Room;
import com.example.PlanR.repository.RoomRepository;

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
        if(dto.getHasComputers())
            room.setHasComputers(dto.getHasComputers());
        else
            room.setHasComputers(false);
        
        if(dto.getHasHardwareKits())
            room.setHasHardwareKits(dto.getHasHardwareKits());
        else
            room.setHasHardwareKits(false);
        room.setFloorNumber(dto.getFloorNumber());
        room.setBlock(dto.getBlock());
        if (dto.getDept() != null) {
            room.setDept(dto.getDept().getShortCode());
            room.setDepartment(dto.getDept());
        }

        Room savedRoom = roomRepository.save(room);
        return ResponseEntity.ok(savedRoom);
    }
}
