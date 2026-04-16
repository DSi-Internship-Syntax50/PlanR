package com.example.PlanR.controller;

import com.example.PlanR.dto.SeatPlanRequestDto;
import com.example.PlanR.dto.SeatPlanResponseDto;
import com.example.PlanR.service.SeatPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seatplan")
public class SeatPlanApiController {

    @Autowired
    private SeatPlanService seatPlanService;

    @PostMapping("/generate")
    public ResponseEntity<SeatPlanResponseDto> generate(@RequestBody SeatPlanRequestDto request) {
        SeatPlanResponseDto response = seatPlanService.generateSeatPlan(request);
        return ResponseEntity.ok(response);
    }

    @Autowired
    private com.example.PlanR.service.SeatPlanPdfService seatPlanPdfService;

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
