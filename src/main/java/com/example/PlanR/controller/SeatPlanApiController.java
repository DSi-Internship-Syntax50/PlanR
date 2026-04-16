package com.example.PlanR.controller;

import com.example.PlanR.dto.SeatPlanRequestDto;
import com.example.PlanR.dto.SeatPlanResponseDto;
import com.example.PlanR.service.SeatPlanService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seatplan")
public class SeatPlanApiController {

    private final SeatPlanService seatPlanService;

    public SeatPlanApiController(SeatPlanService seatPlanService) {
        this.seatPlanService = seatPlanService;
    }

    @PostMapping("/generate")
    public ResponseEntity<SeatPlanResponseDto> generate(@RequestBody SeatPlanRequestDto request) {
        SeatPlanResponseDto response = seatPlanService.generateSeatPlan(request);
        return ResponseEntity.ok(response);
    }
}
