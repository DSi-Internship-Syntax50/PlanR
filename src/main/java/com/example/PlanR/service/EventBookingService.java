package com.example.PlanR.service;

import com.example.PlanR.dto.EventBookingRequestDto;
import com.example.PlanR.dto.EventBookingResponseDto;
import com.example.PlanR.model.EventBooking;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.BookingStatus;
import com.example.PlanR.model.enums.EventType;
import com.example.PlanR.repository.EventBookingRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventBookingService {

    @Autowired
    private EventBookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public List<EventBookingResponseDto> getBookingsForMonth(LocalDate targetDate) {
        LocalDate startDate = targetDate.withDayOfMonth(1);
        LocalDate endDate = targetDate.withDayOfMonth(targetDate.lengthOfMonth());
        return bookingRepository.findBySpecificDateBetween(startDate, endDate)
                .stream()
                .map(EventBookingResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public EventBookingResponseDto bookSlot(EventBookingRequestDto requestDto) {
        Room room = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Use default admin if userId not sent for testing purposes
        User requestor = null;
        if (requestDto.getUserId() != null) {
            requestor = userRepository.findById(requestDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
             List<User> users = userRepository.findAll();
             if(!users.isEmpty()) {
                 requestor = users.get(0);
             }
        }

        if (requestDto.getStartTime().isAfter(requestDto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        List<EventBooking> overlaps = bookingRepository.findOverlappingBookings(
                room.getId(),
                requestDto.getSpecificDate(),
                requestDto.getStartTime(),
                requestDto.getEndTime()
        );

        EventBooking newBooking = new EventBooking();
        newBooking.setRoom(room);
        newBooking.setRequestedBy(requestor);
        newBooking.setSpecificDate(requestDto.getSpecificDate());
        newBooking.setStartTime(requestDto.getStartTime());
        newBooking.setEndTime(requestDto.getEndTime());
        newBooking.setEventType(requestDto.getEventType());
        newBooking.setTitle(requestDto.getTitle());
        newBooking.setStatus(BookingStatus.APPROVED); // Auto approve for now

        if (!overlaps.isEmpty()) {
             // Let's check priority override logic
             // Rule: ACADEMIC overrides EXTRACURRICULAR (CLUB)
             if (requestDto.getEventType() == EventType.ACADEMIC) {
                 boolean canOverrideAll = true;
                 for (EventBooking overlap : overlaps) {
                     if (overlap.getEventType() == EventType.ACADEMIC || overlap.getEventType() == EventType.EXAM) {
                         canOverrideAll = false;
                         break; // Can't override another high priority event
                     }
                 }

                 if (canOverrideAll) {
                     // Displace existing events
                     bookingRepository.save(newBooking); // Save first to get ID
                     for (EventBooking overlap : overlaps) {
                         overlap.setStatus(BookingStatus.DISPLACED);
                         overlap.setDisplacedByEventId(newBooking.getId());
                         bookingRepository.save(overlap);
                     }
                     return new EventBookingResponseDto(newBooking);
                 } else {
                     throw new RuntimeException("Overlapping with another high-priority event.");
                 }
             } else {
                 throw new RuntimeException("Overlapping slots perfectly booked by another event.");
             }
        }

        bookingRepository.save(newBooking);
        return new EventBookingResponseDto(newBooking);
    }
}
