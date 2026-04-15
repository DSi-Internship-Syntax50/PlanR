package com.example.PlanR.controller;

import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.UserRepository;
import com.example.PlanR.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleApiController {

    private final ScheduleService scheduleService;
    private final MasterRoutineRepository routineRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ScheduleApiController(ScheduleService scheduleService, 
                                 MasterRoutineRepository routineRepository,
                                 CourseRepository courseRepository,
                                 UserRepository userRepository) {
        this.scheduleService = scheduleService;
        this.routineRepository = routineRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    // 1. AUTO-GENERATE (Now requires Department ID)
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
                
                int slotCount = rt.getCourse().getRequiredSlots() != null ? rt.getCourse().getRequiredSlots() : (Boolean.TRUE.equals(rt.getCourse().getIsLab()) ? 3 : 1);
                courseDto.put("slotCount", slotCount); 
                dto.put("course", courseDto);
                
                dto.put("teacher", Map.of("id", rt.getTeacher() != null ? rt.getTeacher().getId() : 1)); 
                
                if (rt.getRoom() != null) {
                    dto.put("room", Map.of("roomNumber", rt.getRoom().getRoomNumber()));
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
            
            int slotCount = c.getRequiredSlots() != null ? c.getRequiredSlots() : (Boolean.TRUE.equals(c.getIsLab()) ? 3 : 1);
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
            @RequestParam int startSlotIndex) {
        try {
            Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Invalid Course ID"));
            User teacher = userRepository.findById(teacherId).orElse(null); 
            MasterRoutine routine = new MasterRoutine();
            routine.setCourse(course);
            routine.setTeacher(teacher);
            routine.setDayOfWeek(dayOfWeek);
            routine.setStartSlotIndex(startSlotIndex);
            routineRepository.save(routine);
            return ResponseEntity.ok(Map.of("message", "Class scheduled successfully!"));
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