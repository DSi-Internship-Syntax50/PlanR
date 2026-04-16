package com.example.PlanR.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PlanR.dto.EventBookingRequestDto;
import com.example.PlanR.dto.EventBookingResponseDto;
import com.example.PlanR.dto.RoomOccupancySlot;
import com.example.PlanR.exception.EntityNotFoundException;
import com.example.PlanR.exception.ValidationException;
import com.example.PlanR.model.ClassOverride;
import com.example.PlanR.model.EventBooking;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.BookingStatus;
import com.example.PlanR.model.enums.DayOfWeek;
import com.example.PlanR.model.enums.OverrideStatus;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.ClassOverrideRepository;
import com.example.PlanR.repository.EventBookingRepository;
import com.example.PlanR.repository.MasterRoutineRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;

/**
 * Service for event booking operations.
 * Merged: constructor injection + typed exceptions (our refactoring)
 *       + room occupancy grid and ClassOverride support (other branch).
 */
@Service
public class EventBookingService {

    private final EventBookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MasterRoutineRepository masterRoutineRepository;
    private final ClassOverrideRepository classOverrideRepository;

    public EventBookingService(EventBookingRepository bookingRepository,
                               RoomRepository roomRepository,
                               UserRepository userRepository,
                               MasterRoutineRepository masterRoutineRepository,
                               ClassOverrideRepository classOverrideRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.masterRoutineRepository = masterRoutineRepository;
        this.classOverrideRepository = classOverrideRepository;
    }

    /**
     * Returns a 12-slot hourly occupancy grid (8AM-7PM) for a room on a given date,
     * merging MasterRoutine class schedules and EventBooking records.
     * (From other branch)
     */
    public List<RoomOccupancySlot> getRoomOccupancy(Long roomId, LocalDate date) {
        List<RoomOccupancySlot> grid = new ArrayList<>();
        for (int h = 8; h < 20; h++) {
            grid.add(new RoomOccupancySlot(h));
        }

        java.time.DayOfWeek javaDow = date.getDayOfWeek();
        DayOfWeek dow = DayOfWeek.values()[javaDow.getValue() % 7];

        List<MasterRoutine> routines = masterRoutineRepository.findByRoomIdAndDayOfWeek(roomId, dow);

        List<Long> routineIds = routines.stream().map(MasterRoutine::getId).collect(Collectors.toList());
        Set<Long> cancelledRoutineIds = routineIds.isEmpty() ? Set.of() :
            classOverrideRepository.findBySpecificDateAndRoutineIdIn(date, routineIds)
                .stream()
                .filter(co -> co.getStatus() == OverrideStatus.CANCELLED)
                .map(co -> co.getRoutine().getId())
                .collect(Collectors.toSet());

        // 4. Mark CLASS slots
        // Slot mapping: slot 1→8:00, slot 2→9:30, slot 3→11:00, slot 4→12:30, slot 5→14:00, slot 6→15:30
        // Each slot spans 1.5 hours. Duration is in slots, so total hours = duration * 1.5
        for (MasterRoutine rt : routines) {
            if (cancelledRoutineIds.contains(rt.getId())) continue; // skip cancelled

            int startHour;
            int endHour;

            if (rt.getStartSlotIndex() != null) {
                // Slot index based mapping (from routine builder)
                double startHourFloat = 8.0 + (rt.getStartSlotIndex() - 1) * 1.5;
                startHour = (int) startHourFloat;

                int durationSlots = 1;
                if (rt.getCourse() != null) {
                    if (rt.getCourse().getRequiredSlots() != null) {
                        durationSlots = rt.getCourse().getRequiredSlots();
                    } else if (rt.getCourse().getSlotCount() != null) {
                        durationSlots = rt.getCourse().getSlotCount();
                    } else if (Boolean.TRUE.equals(rt.getCourse().getIsLab())) {
                        durationSlots = 3;
                    }
                }
                double totalHours = durationSlots * 1.5;
                endHour = (int) Math.ceil(startHourFloat + totalHours);
            } else if (rt.getStartTime() != null && rt.getEndTime() != null) {
                // Fallback to time-based for manual/legacy entries
                startHour = rt.getStartTime().getHour();
                endHour = rt.getEndTime().getHour();
                if (rt.getEndTime().getMinute() > 0) endHour++;
            } else {
                continue; // Cannot determine occupancy, skip
            }

            String courseCode = (rt.getCourse() != null) ? rt.getCourse().getCourseCode() : "Class";

            for (int h = startHour; h < endHour; h++) {
                if (h >= 8 && h < 20) {
                    grid.get(h - 8).markAsClass(courseCode);
                }
            }
        }

        List<EventBooking> events = bookingRepository.findByRoomIdAndSpecificDate(roomId, date);
        for (EventBooking ev : events) {
            if (ev.getStartTime() == null || ev.getEndTime() == null) continue;
            int startH = ev.getStartTime().getHour();
            int endH = ev.getEndTime().getHour();
            if (ev.getEndTime().getMinute() > 0) endH++;

            for (int h = startH; h < endH; h++) {
                if (h >= 8 && h < 20) {
                    grid.get(h - 8).markAsEvent(ev.getTitle(), ev.getStatus().name());
                }
            }
        }

        return grid;
    }

    public List<EventBookingResponseDto> getBookingsForMonth(LocalDate targetDate, String username, boolean isAdmin) {
        LocalDate startDate = targetDate.withDayOfMonth(1);
        LocalDate endDate = targetDate.withDayOfMonth(targetDate.lengthOfMonth());
        return bookingRepository.findVisibleBookingsForMonth(startDate, endDate, username, isAdmin)
                .stream()
                .map(EventBookingResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventBookingResponseDto bookSlot(EventBookingRequestDto requestDto, String username) {
        Room room = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room", requestDto.getRoomId()));

        User requestor = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User", username));

        if (requestDto.getStartTime().isAfter(requestDto.getEndTime())) {
            throw new ValidationException("Start time must be before end time");
        }

        List<EventBooking> overlaps = bookingRepository.findOverlappingBookings(
                room.getId(),
                requestDto.getSpecificDate(),
                requestDto.getStartTime(),
                requestDto.getEndTime());

        if (!overlaps.isEmpty()) {
            throw new ValidationException("Slot is already booked by another event.");
        }

        EventBooking newBooking = new EventBooking();
        newBooking.setRoom(room);
        newBooking.setRequestedBy(requestor);
        newBooking.setSpecificDate(requestDto.getSpecificDate());
        newBooking.setStartTime(requestDto.getStartTime());
        newBooking.setEndTime(requestDto.getEndTime());
        newBooking.setEventType(requestDto.getEventType());
        newBooking.setTitle(requestDto.getTitle());
        newBooking.setDepartmentName(requestDto.getDepartmentName());
        newBooking.setTeacherName(requestDto.getTeacherName());
        newBooking.setAdditionalInfo(requestDto.getAdditionalInfo());

        if (requestor.getRole() == Role.SUPERADMIN
                || requestor.getRole() == Role.COORDINATOR) {
            newBooking.setStatus(BookingStatus.APPROVED);
        } else {
            newBooking.setStatus(BookingStatus.PENDING);
        }

        bookingRepository.save(newBooking);
        return new EventBookingResponseDto(newBooking);
    }

    @Transactional
    public void approveBooking(Long bookingId) {
        EventBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }
}