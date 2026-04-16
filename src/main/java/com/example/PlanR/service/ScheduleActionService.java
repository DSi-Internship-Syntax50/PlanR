package com.example.PlanR.service;

import com.example.PlanR.exception.EntityNotFoundException;
import com.example.PlanR.exception.SlotConflictException;
import com.example.PlanR.model.Course;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.ScheduleRequest;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.RequestStatus;
import com.example.PlanR.model.enums.RequestType;
import com.example.PlanR.repository.CourseRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.ScheduleRequestRepository;
import com.example.PlanR.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for schedule action operations (allocate, reschedule, request/approve).
 * Refactored to use constructor injection and typed exceptions.
 */
@Service
public class ScheduleActionService {

    private final MasterRoutineRepository routineRepository;
    private final RoomRepository roomRepository;
    private final ScheduleRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ScheduleActionService(MasterRoutineRepository routineRepository,
                                  RoomRepository roomRepository,
                                  ScheduleRequestRepository requestRepository,
                                  UserRepository userRepository,
                                  CourseRepository courseRepository) {
        this.routineRepository = routineRepository;
        this.roomRepository = roomRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public MasterRoutine allocateClass(Long courseId, Long teacherId, Long roomId, DayOfWeek day, int startSlotIndex) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course", courseId));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher", teacherId));

        String batch = course.getBatch();
        int slotCount = course.getSlotCount();

        List<MasterRoutine> conflicts = routineRepository.findBatchConflicts(batch, day, startSlotIndex, slotCount);
        if (!conflicts.isEmpty()) {
            throw new SlotConflictException(batch, day, startSlotIndex, slotCount, conflicts);
        }

        MasterRoutine routine = new MasterRoutine();
        routine.setCourse(course);
        routine.setTeacher(teacher);
        routine.setDayOfWeek(day);
        routine.setStartSlotIndex(startSlotIndex);

        if (roomId != null) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new EntityNotFoundException("Room", roomId));
            routine.setRoom(room);
        }

        return routineRepository.save(routine);
    }

    public MasterRoutine allocateRoom(Long routineId, Long roomId) {
        MasterRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new EntityNotFoundException("Routine", routineId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room", roomId));

        routine.setRoom(room);
        return routineRepository.save(routine);
    }

    public MasterRoutine reschedule(Long routineId, DayOfWeek newDay, Integer newStartSlot, Long newRoomId) {
        MasterRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new EntityNotFoundException("Routine", routineId));

        routine.setDayOfWeek(newDay);
        routine.setStartSlotIndex(newStartSlot);

        if (newRoomId != null) {
            Room room = roomRepository.findById(newRoomId)
                    .orElseThrow(() -> new EntityNotFoundException("Room", newRoomId));
            routine.setRoom(room);
        }

        return routineRepository.save(routine);
    }

    public ScheduleRequest requestAction(Long routineId, Long requesterId, RequestType requestType,
            Long requestedRoomId, DayOfWeek requestedDay, Integer requestedStartSlot) {
        MasterRoutine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new EntityNotFoundException("Routine", routineId));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User", requesterId));

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
                .orElseThrow(() -> new EntityNotFoundException("ScheduleRequest", requestId));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("User", approverId));

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
            reschedule(request.getRoutineSchedule().getId(), request.getRequestedDayOfWeek(),
                    request.getRequestedStartSlotIndex(), roomId);
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setApprover(approver);
        return requestRepository.save(request);
    }
}