package com.example.PlanR.dto;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.enums.DayOfWeek;

/**
 * DTO for routine data used across multiple API endpoints.
 * Refactored to use SlotCalculator for consistent slot count computation.
 */
public class RoutineDTO {
    public Long id;
    public String courseCode;
    public String courseTitle;
    public Integer studentCapacity;
    public String teacherInitials;
    public DayOfWeek dayOfWeek;
    public Integer startSlotIndex;
    public Integer slotCount;
    public Long roomId;
    public String roomName;

    public RoutineDTO() {}

    public RoutineDTO(MasterRoutine routine) {
        this.id = routine.getId();
        this.courseCode = routine.getCourse() != null ? routine.getCourse().getCourseCode() : null;
        this.courseTitle = routine.getCourse() != null ? routine.getCourse().getTitle() : null;
        this.studentCapacity = routine.getCourse() != null ? routine.getCourse().getStudentCapacity() : null;
        this.teacherInitials = resolveTeacherInitials(routine);
        this.dayOfWeek = routine.getDayOfWeek();
        this.startSlotIndex = routine.getStartSlotIndex();
        this.slotCount = routine.getCourse() != null
                ? SlotCalculator.getEffectiveSlotCount(routine.getCourse()) : 1;
        
        if (routine.getRoom() != null) {
            this.roomId = routine.getRoom().getId();
            this.roomName = routine.getRoom().getRoomNumber();
        }
    }

    private static String resolveTeacherInitials(MasterRoutine routine) {
        String name = null;
        if (routine.getTeacher() != null && routine.getTeacher().getName() != null) {
            name = routine.getTeacher().getName();
        } else if (routine.getCourse() != null && routine.getCourse().getTeacher() != null
                && routine.getCourse().getTeacher().getName() != null) {
            name = routine.getCourse().getTeacher().getName();
        }

        if (name == null) return "TBA";

        StringBuilder initials = new StringBuilder();
        for (String part : name.split(" ")) {
            if (!part.isEmpty()) initials.append(part.charAt(0));
        }
        return initials.toString().toUpperCase();
    }
}
