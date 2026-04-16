package com.example.PlanR.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.dto.RoomCreateDto;
import com.example.PlanR.service.DepartmentService;
import com.example.PlanR.service.RoomService;

import jakarta.validation.Valid;

/**
 * REST API for room management.
 * Refactored to use constructor injection.
 * Includes CRUD operations from both branches.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final DepartmentService departmentService;

    public RoomController(RoomService roomService, DepartmentService departmentService) {
        this.roomService = roomService;
        this.departmentService = departmentService;
    }

    // --- CREATE ---
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'COORDINATOR')")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomCreateDto dto) {
        Room room = new Room();
        return saveRoomDetails(room, dto);
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'COORDINATOR')")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomCreateDto dto) {
        Room room = roomService.findRoomById(id).orElse(null);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return saveRoomDetails(room, dto);
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'COORDINATOR')")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        if (!roomService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        roomService.deleteRoomById(id);
        return ResponseEntity.ok().build();
    }

    // Shared logic to prevent NullPointerExceptions and handle mapping
    private ResponseEntity<Room> saveRoomDetails(Room room, RoomCreateDto dto) {
        room.setRoomNumber(dto.getRoomNumber());
        room.setType(dto.getType());
        room.setCapacity(dto.getCapacity());
        room.setHasComputers(Boolean.TRUE.equals(dto.getHasComputers()));
        room.setHasHardwareKits(Boolean.TRUE.equals(dto.getHasHardwareKits()));
        room.setFloorNumber(dto.getFloorNumber());
        room.setBlock(dto.getBlock());

        if (dto.getDeptId() != null) {
            Department dept = departmentService.findDepartmentById(dto.getDeptId()).orElse(null);
            if (dept != null) {
                room.setDept(dept.getShortCode());
                room.setDepartment(dept);
            }
        } else {
            room.setDept(null);
            room.setDepartment(null);
        }

        Room savedRoom = roomService.saveRoom(room);
        return ResponseEntity.ok(savedRoom);
    }
}