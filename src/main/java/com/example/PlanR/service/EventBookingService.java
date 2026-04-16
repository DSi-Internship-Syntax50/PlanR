package com.example.PlanR.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.PlanR.dto.EventBookingRequestDto;
import com.example.PlanR.dto.EventBookingResponseDto;
import com.example.PlanR.exception.EntityNotFoundException;
import com.example.PlanR.exception.ValidationException;
import com.example.PlanR.model.EventBooking;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.User;
import com.example.PlanR.model.enums.BookingStatus;
import com.example.PlanR.model.enums.Role;
import com.example.PlanR.repository.EventBookingRepository;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.UserRepository;

/**
 * Service for event booking operations.
 * Refactored to use constructor injection and typed exceptions.
 */
@Service
public class EventBookingService {

    private final EventBookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public EventBookingService(EventBookingRepository bookingRepository,
                               RoomRepository roomRepository,
                               UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
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
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }
}