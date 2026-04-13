package com.example.PlanR.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.ScheduleRequest;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RequestStatus;
import com.example.PlanR.model.enums.RequestType;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.ScheduleRequestRepository;
import com.example.PlanR.repository.UserRepository;


@Service
public class ScheduleActionService {

    @Autowired
    private MasterRoutineRepository routineRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ScheduleRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    public MasterRoutine allocateRoom(Long routineId, Long roomId) {
        MasterRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RuntimeException("Routine not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        routine.setRoom(room);
        return routineRepository.save(routine);
    }

    public MasterRoutine reschedule(Long routineId, DayOfWeek newDay, Integer newStartSlot, Long newRoomId) {
        MasterRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RuntimeException("Routine not found"));
        
        routine.setDayOfWeek(newDay);
        routine.setStartSlotIndex(newStartSlot);
        
        if (newRoomId != null) {
            Room room = roomRepository.findById(newRoomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            routine.setRoom(room);
        }
        
        return routineRepository.save(routine);
    }

    public ScheduleRequest requestAction(Long routineId, Long requesterId, RequestType requestType, 
                                         Long requestedRoomId, DayOfWeek requestedDay, Integer requestedStartSlot) {
        MasterRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RuntimeException("Routine not found"));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ScheduleRequest request = new ScheduleRequest();
        request.setRoutineSchedule(routine);
        request.setRequester(requester);
        request.setRequestType(requestType);
        
        if (requestedRoomId != null) {
            Room room = roomRepository.findById(requestedRoomId).orElse(null);
            request.setRequestedRoom(room);
        }
        
        request.setRequestedDayOfWeek(requestedDay);
        request.setRequestedStartSlotIndex(requestedStartSlot);
        request.setStatus(RequestStatus.PENDING);

        return requestRepository.save(request);
    }

    public ScheduleRequest approveRequest(Long requestId, Long approverId) {
        ScheduleRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }

        if (request.getRequestType() == RequestType.ALLOCATION) {
            if (request.getRequestedRoom() != null) {
                allocateRoom(request.getRoutineSchedule().getId(), request.getRequestedRoom().getId());
            } else {
                throw new RuntimeException("Missing room id for allocation request");
            }
        } else if (request.getRequestType() == RequestType.RESCHEDULE) {
            Long roomId = request.getRequestedRoom() != null ? request.getRequestedRoom().getId() : null;
            reschedule(request.getRoutineSchedule().getId(), request.getRequestedDayOfWeek(), request.getRequestedStartSlotIndex(), roomId);
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setApprover(approver);
        return requestRepository.save(request);
    }
}
