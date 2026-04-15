package com.example.PlanR.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping; // Added missing import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PlanR.dto.RoutineDTO;
import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.ScheduleRequest;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RequestType; // Added missing import
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.service.RecommendationService;
import com.example.PlanR.service.ScheduleActionService;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ScheduleActionService actionService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MasterRoutineRepository routineRepository;

    // Use the top-level RequestDto or rename this inner class to avoid confusion
    public static class ScheduleRequestDto {
        public Long routineId;
        public RequestType requestType;
        public Long requestedRoomId;
        public DayOfWeek requestedDay;
        public Integer requestedStartSlot;
        public Long requesterId;
    }

    // --- NEW ENDPOINTS REQUIRED FOR ROUTINE BUILDER FRONTEND ---

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @GetMapping("/routine/{batch}")
    public ResponseEntity<List<MasterRoutine>> getRoutineByBatch(@PathVariable String batch) {
        return ResponseEntity.ok(routineRepository.findAllByCourseBatchOrderByDayOfWeekAscStartSlotIndexAsc(batch));
    }

    @DeleteMapping("/routine/{id}")
    public ResponseEntity<Void> freeSlot(@PathVariable Long id) {
        routineRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Removed the second (duplicate) declaration of routineRepository that was here

    // -----------------------------------------------------------

    @GetMapping("/suggest-rooms")
    public ResponseEntity<List<RecommendationService.RoomRecommendation>> getRoomSuggestions(
            @RequestParam Long courseId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam Integer startSlotIndex) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<RecommendationService.RoomRecommendation> suggestions = recommendationService.recommendRooms(course,
                dayOfWeek, startSlotIndex);

        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/allocate-class")
    public ResponseEntity<MasterRoutine> allocateClass(
            @RequestParam Long courseId,
            @RequestParam Long teacherId,
            @RequestParam(required = false) Long roomId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam int startSlotIndex) {

        MasterRoutine routine = actionService.allocateClass(courseId, teacherId, roomId, dayOfWeek, startSlotIndex);
        return ResponseEntity.ok(routine);
    }

    // --- Room Routines Endpoint ---

    @GetMapping("/routines/room/{roomId}")
    public ResponseEntity<List<RoutineDTO>> getRoutinesByRoom(@PathVariable Long roomId) {
        List<RoutineDTO> routines = routineRepository.findByRoomId(roomId)
                .stream().map(RoutineDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(routines);
    }

    @GetMapping("/routines/unassigned")
    public ResponseEntity<List<RoutineDTO>> getUnassignedRoutines() {
        List<RoutineDTO> routines = routineRepository.findByRoomIsNull()
                .stream().map(RoutineDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(routines);
    }

    // --- Direct Execution (Admin / Coordinator) ---

    @PostMapping("/allocate")
    public ResponseEntity<MasterRoutine> allocateRoom(@RequestParam Long routineId, @RequestParam Long roomId) {
        MasterRoutine routine = actionService.allocateRoom(routineId, roomId);
        return ResponseEntity.ok(routine);
    }

    @PostMapping("/reschedule")
    public ResponseEntity<MasterRoutine> reschedule(
            @RequestParam Long routineId,
            @RequestParam DayOfWeek newDay,
            @RequestParam Integer newStartSlot,
            @RequestParam(required = false) Long newRoomId) {
        MasterRoutine routine = actionService.reschedule(routineId, newDay, newStartSlot, newRoomId);
        return ResponseEntity.ok(routine);
    }

    @PostMapping("/requests")
    public ResponseEntity<ScheduleRequest> submitRequest(@RequestBody ScheduleRequestDto request) {
        ScheduleRequest created = actionService.requestAction(
                request.routineId,
                request.requesterId,
                request.requestType,
                request.requestedRoomId,
                request.requestedDay,
                request.requestedStartSlot);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<ScheduleRequest> approveRequest(
            @PathVariable Long requestId,
            @RequestParam Long approverId) {
        ScheduleRequest approved = actionService.approveRequest(requestId, approverId);
        return ResponseEntity.ok(approved);
    }
}