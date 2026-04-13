package com.example.PlanR.model;

import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RequestStatus;
import com.example.PlanR.model.enums.RequestType;
import jakarta.persistence.*;

@Entity
@Table(name = "schedule_requests")
public class ScheduleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type")
    private RequestType requestType;

    @ManyToOne
    @JoinColumn(name = "routine_id")
    private MasterRoutine routineSchedule;

    @ManyToOne
    @JoinColumn(name = "requested_room_id")
    private Room requestedRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_day_of_week")
    private DayOfWeek requestedDayOfWeek;

    @Column(name = "requested_start_slot_index")
    private Integer requestedStartSlotIndex;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver;

    public ScheduleRequest() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public MasterRoutine getRoutineSchedule() {
        return routineSchedule;
    }

    public void setRoutineSchedule(MasterRoutine routineSchedule) {
        this.routineSchedule = routineSchedule;
    }

    public Room getRequestedRoom() {
        return requestedRoom;
    }

    public void setRequestedRoom(Room requestedRoom) {
        this.requestedRoom = requestedRoom;
    }

    public DayOfWeek getRequestedDayOfWeek() {
        return requestedDayOfWeek;
    }

    public void setRequestedDayOfWeek(DayOfWeek requestedDayOfWeek) {
        this.requestedDayOfWeek = requestedDayOfWeek;
    }

    public Integer getRequestedStartSlotIndex() {
        return requestedStartSlotIndex;
    }

    public void setRequestedStartSlotIndex(Integer requestedStartSlotIndex) {
        this.requestedStartSlotIndex = requestedStartSlotIndex;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }
}
