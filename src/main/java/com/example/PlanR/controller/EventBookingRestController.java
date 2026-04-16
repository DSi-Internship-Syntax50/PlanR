package com.example.PlanR.controller;

import com.example.PlanR.dto.EventBookingRequestDto;
import com.example.PlanR.dto.EventBookingResponseDto;
import com.example.PlanR.dto.RoomOccupancySlot;
import com.example.PlanR.service.EventBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class EventBookingRestController {

    @Autowired
    private EventBookingService eventBookingService;

    @GetMapping("/bookings")
    public ResponseEntity<List<EventBookingResponseDto>> getBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (month == null) {
            month = LocalDate.now();
        }
        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINATOR") || a.getAuthority().equals("ROLE_SUPERADMIN"));
        String username = userDetails != null ? userDetails.getUsername() : "";
        return ResponseEntity.ok(eventBookingService.getBookingsForMonth(month, username, isAdmin));
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody EventBookingRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) return ResponseEntity.status(401).build();
            EventBookingResponseDto response = eventBookingService.bookSlot(requestDto, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage()); // Conflict
        }
    }
    
    @PostMapping("/bookings/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINATOR") || a.getAuthority().equals("ROLE_SUPERADMIN"));
        if (!isAdmin) return ResponseEntity.status(403).build();
        
        try {
            eventBookingService.approveBooking(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/bookings/room-occupancy")
    public ResponseEntity<List<RoomOccupancySlot>> getRoomOccupancy(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(eventBookingService.getRoomOccupancy(roomId, date));
    }
}
