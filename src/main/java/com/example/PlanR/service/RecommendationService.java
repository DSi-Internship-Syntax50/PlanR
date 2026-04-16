package com.example.PlanR.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.PlanR.dto.SlotCalculator;
import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;

/**
 * Service for recommending optimal rooms based on constraints.
 * Refactored to use constructor injection.
 */
@Service
public class RecommendationService {

    private final RoomRepository roomRepository;
    private final MasterRoutineRepository routineRepository;

    public RecommendationService(RoomRepository roomRepository, MasterRoutineRepository routineRepository) {
        this.roomRepository = roomRepository;
        this.routineRepository = routineRepository;
    }

    public static class RoomRecommendation {
        public Room room;
        public int penaltyScore;

        public RoomRecommendation(Room room, int penaltyScore) {
            this.room = room;
            this.penaltyScore = penaltyScore;
        }
    }

    public List<RoomRecommendation> recommendRooms(Course course, DayOfWeek dayOfWeek, int startSlotIndex) {
        int endSlotIndex = startSlotIndex + course.getSlotCount() - 1;

        // Strict filters
        List<Room> allRooms = roomRepository.findAll();
        List<Room> validRooms = new ArrayList<>();

        for (Room room : allRooms) {
            // Check capacity
            boolean capacityOk = room.getCapacity() != null && course.getStudentCapacity() != null
                    && room.getCapacity() >= course.getStudentCapacity();

            // Check room type
            boolean roomTypeOk;
            if (course.getIsLab() != null && course.getIsLab()) {
                roomTypeOk = room.getType() == com.example.PlanR.model.enums.RoomType.LAB;
            } else {
                roomTypeOk = room.getType() == com.example.PlanR.model.enums.RoomType.THEORY;
            }

            if (capacityOk && roomTypeOk) {
                // Check overlaps
                List<MasterRoutine> overlaps = routineRepository.findOverlappingRoutines(room.getId(), dayOfWeek,
                        startSlotIndex, endSlotIndex);
                if (overlaps.isEmpty()) {
                    validRooms.add(room);
                }
            }
        }

        // Soft constraints (Ranking logic)
        List<MasterRoutine> teacherRoutines = new ArrayList<>();
        if (course.getTeacher() != null) {
            teacherRoutines = routineRepository
                    .findByTeacherIdAndDayOfWeekOrderByStartSlotIndexAsc(course.getTeacher().getId(), dayOfWeek);
        }

        // Find Prev and Next
        MasterRoutine prevClass = null;
        MasterRoutine nextClass = null;

        for (MasterRoutine routine : teacherRoutines) {
            int routineEnd = routine.getStartSlotIndex() + SlotCalculator.getEffectiveSlotCount(routine.getCourse()) - 1;
            if (routineEnd < startSlotIndex) {
                if (prevClass == null
                        || (prevClass.getStartSlotIndex() + SlotCalculator.getEffectiveSlotCount(prevClass.getCourse()) - 1) < routineEnd) {
                    prevClass = routine;
                }
            }
            if (routine.getStartSlotIndex() > endSlotIndex) {
                if (nextClass == null || nextClass.getStartSlotIndex() > routine.getStartSlotIndex()) {
                    nextClass = routine;
                }
            }
        }

        List<RoomRecommendation> recommendations = new ArrayList<>();
        for (Room room : validRooms) {
            int score = 0;
            if (prevClass != null && prevClass.getRoom() != null) {
                score += calculatePenalty(prevClass.getRoom(), room);
            }
            if (nextClass != null && nextClass.getRoom() != null) {
                score += calculatePenalty(room, nextClass.getRoom());
            }
            recommendations.add(new RoomRecommendation(room, score));
        }

        // Sort by lowest penalty
        recommendations.sort(Comparator.comparingInt(r -> r.penaltyScore));

        return recommendations;
    }

    private int calculatePenalty(Room from, Room to) {
        if (from.getId().equals(to.getId())) {
            return 0; // Same room
        }

        Integer fromFloor = from.getFloorNumber();
        Integer toFloor = to.getFloorNumber();
        String fromBlock = from.getBlock();
        String toBlock = to.getBlock();

        // Assume missing mappings are high penalty safely
        if (fromFloor == null || toFloor == null || fromBlock == null || toBlock == null) {
            return 10;
        }

        if (!fromFloor.equals(toFloor)) {
            return 10; // Floor change
        } else if (!fromBlock.equals(toBlock)) {
            return 5; // Block change
        } else {
            return 1; // Room change
        }
    }
}
