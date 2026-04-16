package com.example.PlanR.service.scheduling;

import java.util.List;

import com.example.PlanR.dto.SlotCalculator;
import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RoomType;

/**
 * Encapsulates room-matching logic for the auto-generation algorithm.
 * Extracted from ScheduleService.assignCourseWithRoom() to separate
 * room selection from scheduling orchestration.
 */
public class RoomMatcher {

    /**
     * Finds an available room of the correct type and department for a course
     * at a specific day/slot combination.
     *
     * @return a matching Room, or null if none available
     */
    public Room findAvailableRoom(Course course, DayOfWeek day, int startSlot, int slotsNeeded,
                                   List<Room> allRooms, List<MasterRoutine> allRoutines) {
        for (Room room : allRooms) {
            if (!isRoomTypeCompatible(course, room)) continue;
            if (!isDepartmentCompatible(course, room)) continue;
            if (isRoomFree(room, day, startSlot, slotsNeeded, allRoutines)) {
                return room;
            }
        }
        return null;
    }

    private boolean isRoomTypeCompatible(Course course, Room room) {
        if (Boolean.TRUE.equals(course.getIsLab()) && room.getType() != RoomType.LAB) return false;
        if (!Boolean.TRUE.equals(course.getIsLab()) && room.getType() != RoomType.THEORY && room.getType() != RoomType.SEMINAR)
            return false;
        return true;
    }

    private boolean isDepartmentCompatible(Course course, Room room) {
        if (room.getDepartment() != null && course.getDepartment() != null) {
            return room.getDepartment().getId().equals(course.getDepartment().getId());
        } else if (room.getDept() != null && course.getDepartment() != null) {
            // Fallback to String-based matching for legacy data
            return room.getDept().equals(course.getDepartment().getShortCode());
        }
        return true; // No department constraint
    }

    private boolean isRoomFree(Room room, DayOfWeek day, int startSlot, int slotsNeeded,
                                List<MasterRoutine> allRoutines) {
        for (MasterRoutine rt : allRoutines) {
            if (rt.getRoom() != null && rt.getRoom().getId().equals(room.getId()) && rt.getDayOfWeek() == day) {
                int rtStart = rt.getStartSlotIndex();
                int rtLength = SlotCalculator.getEffectiveSlotCount(rt.getCourse());

                // Overlap check
                if (startSlot < rtStart + rtLength && startSlot + slotsNeeded > rtStart) {
                    return false;
                }
            }
        }
        return true;
    }
}
