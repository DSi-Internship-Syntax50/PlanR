package com.example.PlanR.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PlanR.exception.SlotConflictException;
import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.service.ScheduleActionService;
import com.example.PlanR.service.ScheduleService;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleApiController {

    private final ScheduleService scheduleService;
    private final ScheduleActionService scheduleActionService;
    private final MasterRoutineRepository routineRepository;
    private final CourseRepository courseRepository;

    public ScheduleApiController(ScheduleService scheduleService, 
                                 ScheduleActionService scheduleActionService,
                                 MasterRoutineRepository routineRepository,
                                 CourseRepository courseRepository) {
        this.scheduleService = scheduleService;
        this.scheduleActionService = scheduleActionService;
        this.routineRepository = routineRepository;
        this.courseRepository = courseRepository;
    }

    // 1. AUTO-GENERATE
    @PostMapping("/auto-generate")
    public ResponseEntity<?> autoGenerate(@RequestParam Long departmentId, @RequestParam String batch) {
        try {
            scheduleService.autoGenerateRoutine(departmentId, batch);
            return ResponseEntity.ok().body(Map.of("message", "Routine successfully generated!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // 2. FETCH ROUTINE TO PAINT THE GRID
    @GetMapping("/routine")
    public ResponseEntity<List<Map<String, Object>>> getRoutineForBatch(@RequestParam Long departmentId, @RequestParam String batch) {
        List<MasterRoutine> routines = routineRepository.findAll(); 
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (MasterRoutine rt : routines) {
            if (rt.getCourse() != null 
                && batch.equals(rt.getCourse().getBatch()) 
                && rt.getCourse().getDepartment() != null 
                && rt.getCourse().getDepartment().getId().equals(departmentId)) {
                
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", rt.getId());
                dto.put("dayOfWeek", rt.getDayOfWeek().name());
                dto.put("startSlotIndex", rt.getStartSlotIndex());
                
                Map<String, Object> courseDto = new HashMap<>();
                courseDto.put("courseCode", rt.getCourse().getCourseCode());
                courseDto.put("title", rt.getCourse().getTitle());
                courseDto.put("isLab", rt.getCourse().getIsLab());
                
                int slotCount = rt.getCourse().getSlotCount();
                courseDto.put("slotCount", slotCount); 
                dto.put("course", courseDto);
                
                dto.put("teacher", Map.of("id", rt.getTeacher() != null ? rt.getTeacher().getId() : 1)); 
                
                // FIXED: Now mapping the room's floor number and block to the JSON
                if (rt.getRoom() != null) {
                    Map<String, Object> roomDto = new HashMap<>();
                    roomDto.put("id", rt.getRoom().getId());
                    roomDto.put("roomNumber", rt.getRoom().getRoomNumber());
                    roomDto.put("floorNumber", rt.getRoom().getFloorNumber());
                    roomDto.put("block", rt.getRoom().getBlock());
                    
                    dto.put("room", roomDto);
                }
                response.add(dto);
            }
        }
        return ResponseEntity.ok(response);
    }

    // 3. FETCH COURSES FOR THE MODAL DROPDOWN
    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourses(@RequestParam Long departmentId, @RequestParam String batch) {
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> batch.equals(c.getBatch()) && c.getDepartment() != null && c.getDepartment().getId().equals(departmentId))
                .collect(Collectors.toList());
                
        List<Map<String, Object>> response = new ArrayList<>();
        
        for(Course c : courses) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", c.getId());
            dto.put("courseCode", c.getCourseCode());
            dto.put("title", c.getTitle());
            
            int slotCount = c.getSlotCount();
            dto.put("slotCount", slotCount);
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }

    // 4. MANUALLY ALLOCATE
    @PostMapping("/allocate-class")
    public ResponseEntity<?> allocateClass(
            @RequestParam Long courseId,
            @RequestParam Long teacherId, 
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam int startSlotIndex,
            @RequestParam(required = false) Long roomId) { // Allow optional Room ID
        try {
            // Using your existing Action Service to handle conflicts properly!
            scheduleActionService.allocateClass(courseId, teacherId, roomId, dayOfWeek, startSlotIndex);
            return ResponseEntity.ok(Map.of("message", "Class scheduled successfully!"));
            
        } catch (SlotConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // 5. DELETE A ROUTINE SLOT
    @DeleteMapping("/routine/{id}")
    public ResponseEntity<?> deleteRoutine(@PathVariable Long id) {
        routineRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Slot freed successfully"));
    }
    @PostMapping("/unassign/{routineId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPERADMIN', 'COORDINATOR')")
    public ResponseEntity<?> unassignRoom(@PathVariable Long routineId) {
        MasterRoutine routine = routineRepository.findById(routineId).orElse(null);
        if (routine != null) {
            routine.setRoom(null); // Remove the room allocation
            routineRepository.save(routine);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Routine not found");
    }
}