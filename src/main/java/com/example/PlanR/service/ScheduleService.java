package com.example.PlanR.service;

import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RoomType;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final CourseRepository courseRepository;
    private final MasterRoutineRepository routineRepository;
    private final RoomRepository roomRepository;

    public ScheduleService(CourseRepository courseRepository, MasterRoutineRepository routineRepository,
            RoomRepository roomRepository) {
        this.courseRepository = courseRepository;
        this.routineRepository = routineRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public void autoGenerateRoutine(Long departmentId, String batch) {
        // 1. Fetch REAL courses for this batch AND department
        List<Course> coursesToAssign = courseRepository.findAll().stream()
                .filter(c -> batch.equals(c.getBatch()) && c.getDepartment() != null
                        && c.getDepartment().getId().equals(departmentId))
                .collect(Collectors.toList());

        if (coursesToAssign.isEmpty()) {
            throw new RuntimeException("No courses found in the database for batch: " + batch
                    + " in this department. Please add them in the Course Manager first.");
        }

        // 2. Fetch all available rooms and currently scheduled routines
        List<Room> allRooms = roomRepository.findAll();
        List<MasterRoutine> allRoutines = new ArrayList<>(routineRepository.findAll());

        // 2.5 Clear old routines for this batch and department to prevent conflicts
        List<MasterRoutine> oldRoutines = allRoutines.stream()
                .filter(rt -> rt.getCourse() != null && batch.equals(rt.getCourse().getBatch())
                        && rt.getCourse().getDepartment() != null
                        && rt.getCourse().getDepartment().getId().equals(departmentId))
                .collect(Collectors.toList());
        routineRepository.deleteAll(oldRoutines);
        allRoutines.removeAll(oldRoutines);

        // 3. Setup the Memory Grid
        boolean[][] batchOccupiedGrid = new boolean[7][13];
        for (MasterRoutine rt : allRoutines) {
            if (rt.getCourse() != null && batch.equals(rt.getCourse().getBatch())
                    && rt.getCourse().getDepartment() != null
                    && rt.getCourse().getDepartment().getId().equals(departmentId)) {

                int dayIndex = rt.getDayOfWeek().ordinal();
                int start = rt.getStartSlotIndex();
                int length = rt.getCourse().getRequiredSlots() != null ? rt.getCourse().getRequiredSlots()
                        : (Boolean.TRUE.equals(rt.getCourse().getIsLab()) ? 3 : 1);

                for (int i = 0; i < length; i++) {
                    if (start + i <= 12)
                        batchOccupiedGrid[dayIndex][start + i] = true;
                }
            }
        }

        // 4. Place courses using Load Balancing
        DayOfWeek[] days = { DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY };

        for (Course course : coursesToAssign) {
            int slotsNeeded = course.getRequiredSlots() != null ? course.getRequiredSlots()
                    : (Boolean.TRUE.equals(course.getIsLab()) ? 3 : 1);
            int timesPerWeek = course.getWeeklyClasses() != null ? course.getWeeklyClasses()
                    : (Boolean.TRUE.equals(course.getIsLab()) ? 1 : 2);

            assignCourseWithRoom(course, days, slotsNeeded, timesPerWeek, batchOccupiedGrid, allRooms, allRoutines);
        }
    }

    private void assignCourseWithRoom(Course course, DayOfWeek[] days, int slotsNeeded, int timesPerWeek,
            boolean[][] batchOccupiedGrid, List<Room> allRooms, List<MasterRoutine> allRoutines) {
        List<DayOfWeek> courseAssignedDays = new ArrayList<>();

        for (int t = 0; t < timesPerWeek; t++) {
            List<DayOfWeek> availableDays = new ArrayList<>();
            for (DayOfWeek day : days) {
                if (!courseAssignedDays.contains(day))
                    availableDays.add(day);
            }

            // Sort the days by how empty the batch's schedule is
            availableDays.sort((d1, d2) -> Integer.compare(
                    getDayLoad(batchOccupiedGrid, d1.ordinal()),
                    getDayLoad(batchOccupiedGrid, d2.ordinal())));

            boolean placed = false;

            for (DayOfWeek day : availableDays) {
                int gridRow = day.ordinal();

                for (int slot = 1; slot <= (12 - slotsNeeded + 1); slot++) {
                    // Step A: Check if the students are free
                    boolean batchFree = true;
                    for (int i = 0; i < slotsNeeded; i++) {
                        if (batchOccupiedGrid[gridRow][slot + i]) {
                            batchFree = false;
                            break;
                        }
                    }
                    if (!batchFree)
                        continue;

                    // Step B: Find a free room of the correct type AND department
                    Room selectedRoom = null;
                    for (Room room : allRooms) {
                        // 1. Check Room Type
                        if (Boolean.TRUE.equals(course.getIsLab()) && room.getType() != RoomType.LAB)
                            continue;
                        if (!Boolean.TRUE.equals(course.getIsLab()) && room.getType() != RoomType.THEORY && room.getType() != RoomType.SEMINAR)
                            continue;

                        // 2. Check Department boundary (Strict matching)
                        if (room.getDepartment() != null && course.getDepartment() != null) {
                            if (!room.getDepartment().getId().equals(course.getDepartment().getId())) {
                                continue;
                            }
                        } else if (room.getDept() != null && course.getDepartment() != null) {
                            // Fallback to String-based matching for legacy data
                            if (!room.getDept().equals(course.getDepartment().getShortCode())) {
                                continue;
                            }
                        }

                        // 3. Check if the room is empty at this time
                        if (isRoomFree(room, day, slot, slotsNeeded, allRoutines)) {
                            selectedRoom = room;
                            break;
                        }
                    }

                    // Step C: Save it!
                    if (selectedRoom != null) {
                        for (int i = 0; i < slotsNeeded; i++) {
                            batchOccupiedGrid[gridRow][slot + i] = true;
                        }

                        MasterRoutine routine = new MasterRoutine();
                        routine.setCourse(course);
                        routine.setTeacher(course.getTeacher()); // Ensure teacher is assigned
                        routine.setDayOfWeek(day);
                        routine.setStartSlotIndex(slot);
                        routine.setRoom(selectedRoom); // ASSIGN THE REAL ROOM!

                        routineRepository.save(routine);
                        allRoutines.add(routine); // Add to local memory so next course doesn't take it

                        courseAssignedDays.add(day);
                        placed = true;
                        break;
                    }
                }
                if (placed)
                    break;
            }
        }
    }

    private boolean isRoomFree(Room room, DayOfWeek day, int startSlot, int slotsNeeded,
            List<MasterRoutine> allRoutines) {
        for (MasterRoutine rt : allRoutines) {
            if (rt.getRoom() != null && rt.getRoom().getId().equals(room.getId()) && rt.getDayOfWeek() == day) {
                int rtStart = rt.getStartSlotIndex();
                int rtLength = rt.getCourse().getRequiredSlots() != null ? rt.getCourse().getRequiredSlots()
                        : (Boolean.TRUE.equals(rt.getCourse().getIsLab()) ? 3 : 1);

                // Overlap check formula
                if (startSlot < rtStart + rtLength && startSlot + slotsNeeded > rtStart) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getDayLoad(boolean[][] occupiedGrid, int dayOrdinal) {
        int load = 0;
        for (int i = 1; i <= 12; i++) {
            if (occupiedGrid[dayOrdinal][i])
                load++;
        }
        return load;
    }
}