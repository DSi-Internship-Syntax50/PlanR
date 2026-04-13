package com.example.PlanR.dto;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.enums.DayOfWeek;

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
        
        if (routine.getTeacher() != null && routine.getTeacher().getName() != null) {
            String[] parts = routine.getTeacher().getName().split(" ");
            StringBuilder initials = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty()) initials.append(p.charAt(0));
            }
            this.teacherInitials = initials.toString().toUpperCase();
        } else if (routine.getCourse() != null && routine.getCourse().getTeacher() != null && routine.getCourse().getTeacher().getName() != null) {
            String[] parts = routine.getCourse().getTeacher().getName().split(" ");
            StringBuilder initials = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty()) initials.append(p.charAt(0));
            }
            this.teacherInitials = initials.toString().toUpperCase();
        } else {
            this.teacherInitials = "TBA";
        }
        this.dayOfWeek = routine.getDayOfWeek();
        this.startSlotIndex = routine.getStartSlotIndex();
        this.slotCount = routine.getCourse() != null && routine.getCourse().getSlotCount() != null ? routine.getCourse().getSlotCount() : 1;
        
        if (routine.getRoom() != null) {
            this.roomId = routine.getRoom().getId();
            this.roomName = routine.getRoom().getRoomNumber();
        }
    }
}
