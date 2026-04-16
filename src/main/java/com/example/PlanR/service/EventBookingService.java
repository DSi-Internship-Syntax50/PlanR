package com.example.PlanR.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.PlanR.dto.EventBookingRequestDto;
import com.example.PlanR.dto.EventBookingResponseDto;
import com.example.PlanR.dto.RoomOccupancySlot;
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

@Service
public class EventBookingService {

    @Autowired
    private EventBookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MasterRoutineRepository masterRoutineRepository;

    @Autowired
    private ClassOverrideRepository classOverrideRepository;

    /**
     * Returns a 12-slot hourly occupancy grid (8AM–7PM) for a room on a given date,
     * merging MasterRoutine class schedules and EventBooking records.
     * Slot index → hour: slot N = hour (7 + N), e.g. slot 1 = 8AM, slot 2 = 9AM
     */
    public List<RoomOccupancySlot> getRoomOccupancy(Long roomId, LocalDate date) {
        // Build the 12-slot grid (hours 8..19)
        List<RoomOccupancySlot> grid = new ArrayList<>();
        for (int h = 8; h < 20; h++) {
            grid.add(new RoomOccupancySlot(h));
        }

        // 1. Map date → day of week (using our custom enum ordinal)
        java.time.DayOfWeek javaDow = date.getDayOfWeek();
        DayOfWeek dow = DayOfWeek.values()[javaDow.getValue() % 7]; // Sun=0..Sat=6

        // 2. Fetch class schedules for this room + day
        List<MasterRoutine> routines = masterRoutineRepository.findByRoomIdAndDayOfWeek(roomId, dow);

        // 3. Find any ClassOverrides (cancellations) for this specific date
        List<Long> routineIds = routines.stream().map(MasterRoutine::getId).collect(Collectors.toList());
        Set<Long> cancelledRoutineIds = routineIds.isEmpty() ? Set.of() :
            classOverrideRepository.findBySpecificDateAndRoutineIdIn(date, routineIds)
                .stream()
                .filter(co -> co.getStatus() == OverrideStatus.CANCELLED)
                .map(co -> co.getRoutine().getId())
                .collect(Collectors.toSet());

        // 4. Mark CLASS slots (slot index → hour: slotN = 7+N)
        for (MasterRoutine rt : routines) {
            if (cancelledRoutineIds.contains(rt.getId())) continue; // skip cancelled
            if (rt.getStartSlotIndex() == null) continue;

            int startHour = 7 + rt.getStartSlotIndex(); // slot 1 → 8AM
            int duration = (rt.getCourse() != null && rt.getCourse().getRequiredSlots() != null)
                    ? rt.getCourse().getRequiredSlots()
                    : (rt.getCourse() != null && Boolean.TRUE.equals(rt.getCourse().getIsLab()) ? 3 : 1);
            String courseCode = (rt.getCourse() != null) ? rt.getCourse().getCourseCode() : "Class";

            for (int i = 0; i < duration; i++) {
                int h = startHour + i;
                if (h >= 8 && h < 20) {
                    grid.get(h - 8).markAsClass(courseCode);
                }
            }
        }

        // 5. Overlay EventBookings (events override class display if overlapping)
        List<EventBooking> events = bookingRepository.findByRoomIdAndSpecificDate(roomId, date);
        for (EventBooking ev : events) {
            if (ev.getStartTime() == null || ev.getEndTime() == null) continue;
            int startH = ev.getStartTime().getHour();
            int endH = ev.getEndTime().getHour();
            if (ev.getEndTime().getMinute() > 0) endH++; // round up partial hours

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
        return bookingRepository.findBySpecificDateBetween(startDate, endDate)
                .stream()
                .filter(b -> isAdmin
                        || b.getStatus() == BookingStatus.APPROVED
                        || (b.getRequestedBy() != null && b.getRequestedBy().getEmail().equals(username)))
                .map(EventBookingResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public EventBookingResponseDto bookSlot(EventBookingRequestDto requestDto, String username) {
        Room room = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        User requestor = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (requestDto.getStartTime().isAfter(requestDto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        List<EventBooking> overlaps = bookingRepository.findOverlappingBookings(
                room.getId(),
                requestDto.getSpecificDate(),
                requestDto.getStartTime(),
                requestDto.getEndTime());

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Slot is already booked by another event.");
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

        // Auto-approve if requestor has elevated privileges, else PENDING
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
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }
}