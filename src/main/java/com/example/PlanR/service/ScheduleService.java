package com.example.PlanR.service;

import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {

    private final CourseRepository courseRepository;
    private final MasterRoutineRepository routineRepository;

    public ScheduleService(CourseRepository courseRepository, MasterRoutineRepository routineRepository) {
        this.courseRepository = courseRepository;
        this.routineRepository = routineRepository;
    }

    @Transactional
    public void autoGenerateRoutine(String batch, int theoryCount, int labCount, int theoryClassesPerWeek) {
        List<Course> coursesToAssign = new ArrayList<>();

        // 1. Generate Lab Courses 
        for(int i = 1; i <= labCount; i++) {
            Course c = new Course();
            c.setCourseCode(batch + "-LAB" + i);
            c.setTitle("Auto Generated Lab " + i);
            c.setIsLab(true);
            c.setBatch(batch);
            coursesToAssign.add(courseRepository.save(c));
        }

        // 2. Generate Theory Courses
        for(int i = 1; i <= theoryCount; i++) {
            Course c = new Course();
            c.setCourseCode(batch + "-TH" + i);
            c.setTitle("Auto Generated Theory " + i);
            c.setIsLab(false);
            c.setBatch(batch);
            coursesToAssign.add(courseRepository.save(c));
        }

        // 3. Setup the Memory Grid to track occupied slots
        boolean[][] occupiedGrid = new boolean[7][13]; 

        // 4. Pre-fill grid with existing classes in the DB so we don't overwrite them
        List<MasterRoutine> existingRoutines = routineRepository.findAll();
        for (MasterRoutine rt : existingRoutines) {
            if (rt.getCourse() != null && batch.equals(rt.getCourse().getBatch())) {
                int dayIndex = rt.getDayOfWeek().ordinal();
                int start = rt.getStartSlotIndex();
                int length = Boolean.TRUE.equals(rt.getCourse().getIsLab()) ? 3 : 1;
                
                for (int i = 0; i < length; i++) {
                    if (start + i <= 12) occupiedGrid[dayIndex][start + i] = true;
                }
            }
        }

        // 5. Place the new courses using Load Balancing
        DayOfWeek[] days = {DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY};
        
        for (Course course : coursesToAssign) {
            if (Boolean.TRUE.equals(course.getIsLab())) {
                assignBalancedCourseToGrid(course, days, 3, 1, occupiedGrid);
            } else {
                assignBalancedCourseToGrid(course, days, 1, theoryClassesPerWeek, occupiedGrid);
            }
        }
    }

    private void assignBalancedCourseToGrid(Course course, DayOfWeek[] days, int slotsNeeded, int timesPerWeek, boolean[][] occupiedGrid) {
        List<DayOfWeek> courseAssignedDays = new ArrayList<>();

        for (int t = 0; t < timesPerWeek; t++) {
            // Find available days that don't already have this specific course scheduled today
            List<DayOfWeek> availableDays = new ArrayList<>();
            for (DayOfWeek day : days) {
                if (!courseAssignedDays.contains(day)) {
                    availableDays.add(day);
                }
            }

            // MAGIC HAPPENS HERE: Sort the days by how empty they are!
            availableDays.sort((d1, d2) -> Integer.compare(
                    getDayLoad(occupiedGrid, d1.ordinal()),
                    getDayLoad(occupiedGrid, d2.ordinal())
            ));

            boolean placed = false;

            // Try to place it in the emptiest day first
            for (DayOfWeek day : availableDays) {
                int gridRow = day.ordinal();

                // Look for consecutive empty slots on this day
                for (int slot = 1; slot <= (12 - slotsNeeded + 1); slot++) {
                    boolean isFree = true;
                    for (int i = 0; i < slotsNeeded; i++) {
                        if (occupiedGrid[gridRow][slot + i]) {
                            isFree = false;
                            break;
                        }
                    }

                    // If we found a free block, reserve slots and save to DB
                    if (isFree) {
                        for (int i = 0; i < slotsNeeded; i++) {
                            occupiedGrid[gridRow][slot + i] = true;
                        }

                        MasterRoutine routine = new MasterRoutine();
                        routine.setCourse(course);
                        routine.setDayOfWeek(day);
                        routine.setStartSlotIndex(slot);
                        routineRepository.save(routine);

                        courseAssignedDays.add(day);
                        placed = true;
                        break; // Move on to the next 't' (next class for the week)
                    }
                }
                if (placed) break; // Successfully placed, stop searching days
            }
        }
    }

    // Helper method to count how many slots are already taken on a specific day
    private int getDayLoad(boolean[][] occupiedGrid, int dayOrdinal) {
        int load = 0;
        for (int i = 1; i <= 12; i++) {
            if (occupiedGrid[dayOrdinal][i]) load++;
        }
        return load;
    }
}