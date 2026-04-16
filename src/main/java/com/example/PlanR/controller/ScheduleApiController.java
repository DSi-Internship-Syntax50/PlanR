package com.example.PlanR.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PlanR.dto.CourseDTO;
import com.example.PlanR.exception.SlotConflictException;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.service.RoutineQueryService;
import com.example.PlanR.service.ScheduleActionService;
import com.example.PlanR.service.ScheduleService;

/**
 * REST API for schedule CRUD operations (auto-generate, allocate, fetch, delete).
 * Business logic delegated to services; no inline DTO construction.
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleApiController {

    private final ScheduleService scheduleService;
    private final ScheduleActionService scheduleActionService;
    private final RoutineQueryService routineQueryService;
    private final MasterRoutineRepository routineRepository;

    public ScheduleApiController(ScheduleService scheduleService,
                                 ScheduleActionService scheduleActionService,
                                 RoutineQueryService routineQueryService,
                                 MasterRoutineRepository routineRepository) {
        this.scheduleService = scheduleService;
        this.scheduleActionService = scheduleActionService;
        this.routineQueryService = routineQueryService;
        this.routineRepository = routineRepository;
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
    public ResponseEntity<List<Map<String, Object>>> getRoutineForBatch(
            @RequestParam Long departmentId, @RequestParam String batch) {
        return ResponseEntity.ok(routineQueryService.getRoutinesForBatch(departmentId, batch));
    }

    // 3. FETCH COURSES FOR THE MODAL DROPDOWN
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> getCourses(
            @RequestParam Long departmentId, @RequestParam String batch) {
        return ResponseEntity.ok(routineQueryService.getCoursesForBatch(departmentId, batch));
    }

    // 4. MANUALLY ALLOCATE
    @PostMapping("/allocate-class")
    public ResponseEntity<?> allocateClass(
            @RequestParam Long courseId,
            @RequestParam Long teacherId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam int startSlotIndex,
            @RequestParam(required = false) Long roomId) {
        try {
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
}