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
 * Merged: constructor injection + SlotCalculator (ours)
 *       + optimized findRoomsByCapacityAndType query (other branch).
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

        // Use optimized query from other branch
        com.example.PlanR.model.enums.RoomType roomType = (course.getIsLab() != null && course.getIsLab())
                ? com.example.PlanR.model.enums.RoomType.LAB
                : com.example.PlanR.model.enums.RoomType.THEORY;

        List<Room> validRoomsFiltered = roomRepository.findRoomsByCapacityAndType(course.getStudentCapacity(), roomType);
        List<Room> validRooms = new ArrayList<>();

        for (Room room : validRoomsFiltered) {
            List<MasterRoutine> overlaps = routineRepository.findOverlappingRoutines(room.getId(), dayOfWeek,
                    startSlotIndex, endSlotIndex);
            if (overlaps.isEmpty()) {
                validRooms.add(room);
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

        recommendations.sort(Comparator.comparingInt(r -> r.penaltyScore));

        return recommendations;
    }

    private int calculatePenalty(Room from, Room to) {
        if (from.getId().equals(to.getId())) {
            return 0;
        }

        Integer fromFloor = from.getFloorNumber();
        Integer toFloor = to.getFloorNumber();
        String fromBlock = from.getBlock();
        String toBlock = to.getBlock();

        if (fromFloor == null || toFloor == null || fromBlock == null || toBlock == null) {
            return 10;
        }

        if (!fromFloor.equals(toFloor)) {
            return 10;
        } else if (!fromBlock.equals(toBlock)) {
            return 5;
        } else {
            return 1;
        }
    }
}
