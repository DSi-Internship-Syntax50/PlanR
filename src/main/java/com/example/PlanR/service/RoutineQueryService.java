package com.example.PlanR.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.PlanR.dto.CourseDTO;
import com.example.PlanR.dto.RoutineDTO;
import com.example.PlanR.dto.SlotCalculator;
import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.MasterRoutineRepository;

/**
 * Service centralizing routine/course data fetching and DTO conversion.
 * Eliminates inline HashMap construction in ScheduleApiController.
 */
@Service
public class RoutineQueryService {

    private final MasterRoutineRepository routineRepository;
    private final CourseRepository courseRepository;

    public RoutineQueryService(MasterRoutineRepository routineRepository,
                               CourseRepository courseRepository) {
        this.routineRepository = routineRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * Returns routine data for a specific batch and department, formatted for the grid UI.
     */
    public List<Map<String, Object>> getRoutinesForBatch(Long departmentId, String batch) {
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
                courseDto.put("slotCount", SlotCalculator.getEffectiveSlotCount(rt.getCourse()));
                dto.put("course", courseDto);

                dto.put("teacher", Map.of("id", rt.getTeacher() != null ? rt.getTeacher().getId() : 1));

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
        return response;
    }

    /**
     * Returns courses for a given department and batch, formatted as DTOs.
     */
    public List<CourseDTO> getCoursesForBatch(Long departmentId, String batch) {
        return courseRepository.findAll().stream()
                .filter(c -> batch.equals(c.getBatch())
                        && c.getDepartment() != null
                        && c.getDepartment().getId().equals(departmentId))
                .map(CourseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns routines assigned to a specific room.
     */
    public List<RoutineDTO> getRoutinesByRoom(Long roomId) {
        return routineRepository.findByRoomId(roomId)
                .stream().map(RoutineDTO::new).collect(Collectors.toList());
    }

    /**
     * Returns routines that have no room assigned.
     */
    public List<RoutineDTO> getUnassignedRoutines() {
        return routineRepository.findByRoomIsNull()
                .stream().map(RoutineDTO::new).collect(Collectors.toList());
    }
}
