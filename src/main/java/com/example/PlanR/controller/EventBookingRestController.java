package com.example.PlanR.controller;

import com.example.PlanR.dto.EventBookingRequestDto;
import com.example.PlanR.dto.EventBookingResponseDto;
import com.example.PlanR.service.EventBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class EventBookingRestController {

    @Autowired
    private EventBookingService eventBookingService;

    @GetMapping("/bookings")
    public ResponseEntity<List<EventBookingResponseDto>> getBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        if (month == null) {
            month = LocalDate.now();
        }
        return ResponseEntity.ok(eventBookingService.getBookingsForMonth(month));
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody EventBookingRequestDto requestDto) {
        try {
            EventBookingResponseDto response = eventBookingService.bookSlot(requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage()); // Conflict
        }
    }
}
