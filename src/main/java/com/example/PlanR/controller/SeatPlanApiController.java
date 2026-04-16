package com.example.PlanR.controller;

import com.example.PlanR.dto.SeatPlanRequestDto;
import com.example.PlanR.dto.SeatPlanResponseDto;
import com.example.PlanR.service.SeatPlanService;
import com.example.PlanR.service.SeatPlanPdfService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seatplan")
public class SeatPlanApiController {

    private final SeatPlanService seatPlanService;
    private final SeatPlanPdfService seatPlanPdfService;

    public SeatPlanApiController(SeatPlanService seatPlanService, SeatPlanPdfService seatPlanPdfService) {
        this.seatPlanService = seatPlanService;
        this.seatPlanPdfService = seatPlanPdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<SeatPlanResponseDto> generate(@RequestBody SeatPlanRequestDto request) {
        SeatPlanResponseDto response = seatPlanService.generateSeatPlan(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/pdf/{id}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = seatPlanPdfService.generatePdf(id);
            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=SeatPlan_" + id + ".pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<SeatPlanResponseDto> getByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(seatPlanService.getSeatPlanForRoom(roomId));
    }
}
