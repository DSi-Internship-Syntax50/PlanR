package com.example.PlanR.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.dto.RoomCreateDto;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.RoomRepository;

/**
 * REST API for room management.
 * Refactored to use constructor injection.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;

    public RoomController(RoomRepository roomRepository, DepartmentRepository departmentRepository) {
        this.roomRepository = roomRepository;
        this.departmentRepository = departmentRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'COORDINATOR')")
    public ResponseEntity<Room> createRoom(@RequestBody RoomCreateDto dto) {
        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setType(dto.getType());
        room.setCapacity(dto.getCapacity());
        room.setHasComputers(Boolean.TRUE.equals(dto.getHasComputers()));
        room.setHasHardwareKits(Boolean.TRUE.equals(dto.getHasHardwareKits()));
        room.setFloorNumber(dto.getFloorNumber());
        room.setBlock(dto.getBlock());

        if (dto.getDeptId() != null) {
            Department dept = departmentRepository.findById(dto.getDeptId()).orElse(null);
            if (dept != null) {
                room.setDept(dept.getShortCode());
                room.setDepartment(dept);
            }
        }

        Room savedRoom = roomRepository.save(room);
        return ResponseEntity.ok(savedRoom);
    }
}
