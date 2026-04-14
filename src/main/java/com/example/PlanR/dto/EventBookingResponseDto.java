package com.example.PlanR.dto;

import com.example.PlanR.model.EventBooking;
import com.example.PlanR.model.enums.BookingStatus;
import com.example.PlanR.model.enums.EventType;
import java.time.LocalDate;
import java.time.LocalTime;

public class EventBookingResponseDto {

    private Long id;
    private Long roomId;
    private String roomName;
    private LocalDate specificDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private EventType eventType;
    private BookingStatus status;
    private String title;
    private String departmentName;
    private String teacherName;
    private String additionalInfo;
    private String requestedByUsername;
    private Long displacedByEventId;

    public EventBookingResponseDto() {}

    public EventBookingResponseDto(EventBooking booking) {
        this.id = booking.getId();
        this.roomId = booking.getRoom() != null ? booking.getRoom().getId() : null;
        this.roomName = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : null;
        this.specificDate = booking.getSpecificDate();
        this.startTime = booking.getStartTime();
        this.endTime = booking.getEndTime();
        this.eventType = booking.getEventType();
        this.status = booking.getStatus();
        this.title = booking.getTitle();
        this.departmentName = booking.getDepartmentName();
        this.teacherName = booking.getTeacherName();
        this.additionalInfo = booking.getAdditionalInfo();
        this.requestedByUsername = booking.getRequestedBy() != null ? booking.getRequestedBy().getName() : null;
        this.displacedByEventId = booking.getDisplacedByEventId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(LocalDate specificDate) {
        this.specificDate = specificDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequestedByUsername() {
        return requestedByUsername;
    }

    public void setRequestedByUsername(String requestedByUsername) {
        this.requestedByUsername = requestedByUsername;
    }

    public Long getDisplacedByEventId() {
        return displacedByEventId;
    }

    public void setDisplacedByEventId(Long displacedByEventId) {
        this.displacedByEventId = displacedByEventId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
