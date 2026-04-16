package com.example.PlanR.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.PlanR.model.Department;
import com.example.PlanR.model.Room;
import com.example.PlanR.dto.RoomCreateDto;
import com.example.PlanR.repository.DepartmentRepository;
import com.example.PlanR.repository.RoomRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return saveRoomDetails(room, dto);
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'COORDINATOR')")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        if (!roomRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Note: If a room is actively assigned to a MasterRoutine, 
        // PostgreSQL might block this via foreign key constraints. 
        // This is good for data integrity!
        roomRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Shared logic to prevent NullPointerExceptions and handle mapping
    private ResponseEntity<Room> saveRoomDetails(Room room, RoomCreateDto dto) {
        room.setHasComputers(Boolean.TRUE.equals(dto.getHasComputers()));
        room.setHasHardwareKits(Boolean.TRUE.equals(dto.getHasHardwareKits()));
        
        room.setRoomNumber(dto.getRoomNumber());
        room.setType(dto.getType());
        room.setCapacity(dto.getCapacity());
        room.setFloorNumber(dto.getFloorNumber());
        room.setBlock(dto.getBlock());
        
        if (dto.getDeptId() != null) {
            Department dept = departmentRepository.findById(dto.getDeptId()).orElse(null);
            if (dept != null) {
                room.setDept(dept.getShortCode());
                room.setDepartment(dept);
            }
        } else {
            room.setDept(null);
            room.setDepartment(null);
        }

        Room savedRoom = roomRepository.save(room);
        return ResponseEntity.ok(savedRoom);
    }
}