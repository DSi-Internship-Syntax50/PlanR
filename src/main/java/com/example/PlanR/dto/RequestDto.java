package com.example.PlanR.dto;

import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RequestType;

public class RequestDto {
    public Long routineId;
    public RequestType requestType;
    public Long requestedRoomId;
    public DayOfWeek requestedDay;
    public Integer requestedStartSlot;
    public Long requesterId; 
}
