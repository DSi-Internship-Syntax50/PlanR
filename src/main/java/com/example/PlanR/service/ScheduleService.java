package com.example.PlanR.service;

import com.example.PlanR.dto.SlotCalculator;
import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.service.scheduling.OccupancyGrid;
import com.example.PlanR.service.scheduling.RoomMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for auto-generating class schedules.
 * Uses OccupancyGrid and RoomMatcher for cleaner separation of concerns.
 */
@Service
public class ScheduleService {

    private final CourseRepository courseRepository;
    private final MasterRoutineRepository routineRepository;
    private final RoomRepository roomRepository;
    private final RoomMatcher roomMatcher = new RoomMatcher();

    public ScheduleService(CourseRepository courseRepository, MasterRoutineRepository routineRepository,
            RoomRepository roomRepository) {
        this.courseRepository = courseRepository;
        this.routineRepository = routineRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public void autoGenerateRoutine(Long departmentId, String batch) {
        // 1. Fetch courses for this batch and department
        List<Course> coursesToAssign = courseRepository.findAll().stream()
                .filter(c -> batch.equals(c.getBatch()) && c.getDepartment() != null
                        && c.getDepartment().getId().equals(departmentId))
                .collect(Collectors.toList());

        if (coursesToAssign.isEmpty()) {
            throw new RuntimeException("No courses found in the database for batch: " + batch
                    + " in this department. Please add them in the Course Manager first.");
        }

        // 2. Fetch rooms and current routines
        List<Room> allRooms = roomRepository.findAll();
        List<MasterRoutine> allRoutines = new ArrayList<>(routineRepository.findAll());

        // 3. Clear old routines for this batch/department
        List<MasterRoutine> oldRoutines = allRoutines.stream()
                .filter(rt -> rt.getCourse() != null && batch.equals(rt.getCourse().getBatch())
                        && rt.getCourse().getDepartment() != null
                        && rt.getCourse().getDepartment().getId().equals(departmentId))
                .collect(Collectors.toList());
        routineRepository.deleteAll(oldRoutines);
        allRoutines.removeAll(oldRoutines);

        // 4. Build the occupancy grid from existing routines
        OccupancyGrid grid = buildOccupancyGrid(allRoutines, batch, departmentId);

        // 5. Place courses using load balancing
        DayOfWeek[] days = { DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY };

        for (Course course : coursesToAssign) {
            int slotsNeeded = SlotCalculator.getEffectiveSlotCount(course);
            int timesPerWeek = course.getWeeklyClasses() != null ? course.getWeeklyClasses()
                    : (Boolean.TRUE.equals(course.getIsLab()) ? 1 : 2);

            assignCourseWithRoom(course, days, slotsNeeded, timesPerWeek, grid, allRooms, allRoutines);
        }
    }

    private OccupancyGrid buildOccupancyGrid(List<MasterRoutine> allRoutines, String batch, Long departmentId) {
        OccupancyGrid grid = new OccupancyGrid();
        for (MasterRoutine rt : allRoutines) {
            if (rt.getCourse() != null && batch.equals(rt.getCourse().getBatch())
                    && rt.getCourse().getDepartment() != null
                    && rt.getCourse().getDepartment().getId().equals(departmentId)) {

                int length = SlotCalculator.getEffectiveSlotCount(rt.getCourse());
                grid.markOccupied(rt.getDayOfWeek(), rt.getStartSlotIndex(), length);
            }
        }
        return grid;
    }

    private void assignCourseWithRoom(Course course, DayOfWeek[] days, int slotsNeeded, int timesPerWeek,
            OccupancyGrid grid, List<Room> allRooms, List<MasterRoutine> allRoutines) {

        List<DayOfWeek> courseAssignedDays = new ArrayList<>();

        for (int t = 0; t < timesPerWeek; t++) {
            List<DayOfWeek> availableDays = new ArrayList<>();
            for (DayOfWeek day : days) {
                if (!courseAssignedDays.contains(day))
                    availableDays.add(day);
            }

            // Sort days by how empty the batch's schedule is
            availableDays.sort((d1, d2) -> Integer.compare(grid.getDayLoad(d1), grid.getDayLoad(d2)));

            boolean placed = false;

            for (DayOfWeek day : availableDays) {
                for (int slot = 1; slot <= (12 - slotsNeeded + 1); slot++) {
                    // Check if the students are free
                    if (!grid.isSlotFree(day, slot, slotsNeeded)) continue;

                    // Find a free room
                    Room selectedRoom = roomMatcher.findAvailableRoom(
                            course, day, slot, slotsNeeded, allRooms, allRoutines);

                    if (selectedRoom != null) {
                        grid.markOccupied(day, slot, slotsNeeded);

                        MasterRoutine routine = new MasterRoutine();
                        routine.setCourse(course);
                        routine.setTeacher(course.getTeacher());
                        routine.setDayOfWeek(day);
                        routine.setStartSlotIndex(slot);
                        routine.setRoom(selectedRoom);

                        routineRepository.save(routine);
                        allRoutines.add(routine);

                        courseAssignedDays.add(day);
                        placed = true;
                        break;
                    }
                }
                if (placed) break;
            }
        }
    }
}